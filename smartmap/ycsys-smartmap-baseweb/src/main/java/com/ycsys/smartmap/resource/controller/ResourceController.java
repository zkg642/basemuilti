package com.ycsys.smartmap.resource.controller;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ycsys.smartmap.cluster.utils.ClusterUtils;
import com.ycsys.smartmap.resource.entity.Resource;
import com.ycsys.smartmap.resource.entity.ResourceType;
import com.ycsys.smartmap.resource.service.ResourceService;
import com.ycsys.smartmap.resource.service.ResourceTypeService;
import com.ycsys.smartmap.sys.common.config.Global;
import com.ycsys.smartmap.sys.common.result.Grid;
import com.ycsys.smartmap.sys.common.result.ResponseEx;
import com.ycsys.smartmap.sys.common.utils.BeanExtUtils;
import com.ycsys.smartmap.sys.common.utils.JsonMapper;
import com.ycsys.smartmap.sys.common.utils.StringUtils;
import com.ycsys.smartmap.sys.entity.DictionaryItem;
import com.ycsys.smartmap.sys.entity.PageHelper;
import com.ycsys.smartmap.sys.entity.User;
import com.ycsys.smartmap.sys.util.DataDictionary;

/**
 * 资源 controller
 * 
 * @author liweixiong
 * @date 2016年11月3日
 */
@Controller
@RequestMapping("/resource")
public class ResourceController {
	private static Logger log = Logger.getLogger(ResourceController.class);
	@Autowired
	private ResourceService resourceService;

	@Autowired
	private ResourceTypeService resourceTypeService;

	@RequestMapping("toEdit")
	public String toEdit(String resourceTypeId, Resource resource, Model model) {
		// 修改
		if (resource.getId() != null) {
			resource = resourceService.get(Resource.class, resource.getId());
			model.addAttribute("resource", resource);
		}
		List<ResourceType> lists = resourceTypeService
				.find("from ResourceType t where t.parent != null order by t.createDate asc");
		/*List<ResourceType> lists2 = new ArrayList<ResourceType>();
		// 取最后一层的数据，即是没有 孩子结点的数据
		for (ResourceType rt : lists) {
			List<ResourceType> tempLists = resourceTypeService
					.find("from ResourceType t where 1 = 1 and t.parent.id="
							+ rt.getId());
			if (tempLists == null || tempLists.size() == 0) {
				lists2.add(rt);
			}
		}*/
		
		model.addAttribute("fileType", DataDictionary.getObject("file_type"));
		model.addAttribute("resourceTypeId", resourceTypeId); //用于设置默认的下拉值
		model.addAttribute("resourceTypeLists", lists);
		model.addAttribute("clusterNames", ClusterUtils.lists());
		return "/resource/resource_edit";
	}

	/**
	 * 保存资源
	 * 
	 * @param file
	 * @param resource
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("save")
	@ResponseBody
	public Map<String,String> save(MultipartFile file, Resource resource, Model model,
			HttpServletRequest request) {
		Map<String, String> map = new HashMap<String, String>();
		User user = (User) request.getSession().getAttribute(
				Global.SESSION_USER);
		String path = request.getSession().getServletContext()
				.getRealPath("upload");
		ResourceType resourceType = resourceTypeService.get(ResourceType.class,
				resource.getResourceType().getId());
		// 上传文件处理
		if (file != null && file.getSize() > 0) {
			String fileName = file.getOriginalFilename();
			String fileType = resource.getFileType();
			String tempFileName = fileName.substring(fileName.lastIndexOf(".") + 1);
			//得到数字字典的文件类型
			Map<String, Object> mFileType = DataDictionary.getObject("file_type");
			DictionaryItem dt = (DictionaryItem) mFileType.get(fileType);
			if(!dt.getName().equals(tempFileName)) {
				map.put("flag", "1");
				map.put("msg", "详细分类跟所上传的文件类型不匹配，请重新上传！");
				return map;
			}
			path = path + File.separator + "资源" + File.separator
					+ resourceType.getName();
			File targetFile = new File(path, fileName);
			if (!targetFile.exists()) {
				targetFile.mkdirs();
			}
			try {
				file.transferTo(targetFile);
				resource.setUploadDate(new Date());
				resource.setUploadPerson(user);
				resource.setUploadStatus("1");
				resource.setFilePath(path + File.separator + fileName);
			} catch (IllegalStateException | IOException e) {
				// TODO Auto-generated catch block
				map.put("flag", "2");
				map.put("msg", "上传文件失败！");
				log.warn("上传文件失败：" + e.getMessage());
				return map;
			}
		}
		// 新增
		if (null == resource.getId()) {
			// 得到最大的排序号
			Integer sort = resourceService
					.getMaxSort("select max(r.sort) from Resource r");
			if (null == sort) {
				sort = 0;
			}
			resource.setSort(++sort);
			resource.setCreator(user);
			resource.setCreateDate(new Date());
			resourceService.save(resource);
			map.put("flag", "3");
			map.put("msg", "新增成功！");
		}
		// 修改
		else {
			Resource dbResource = resourceService.get(Resource.class,
					resource.getId());
			try {
				// 得到修改过的属性
				BeanExtUtils.copyProperties(dbResource, resource, true, true,
						null);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dbResource.setUpdateDate(new Date());
			dbResource.setUpdator(user);
			resourceService.update(dbResource);
			map.put("flag", "4");
			map.put("msg", "修改成功！");
		}

		return map;
	}

	/**
	 * 支持删除多条记录
	 * 
	 * @param idsStr
	 * @return
	 */
	@RequestMapping("deletes")
	@ResponseBody
	public Map<String,String> deletes(String idsStr) {
		Map<String,String> map = new HashMap<String,String>();
		String ids[] = idsStr.split(",");
		if (ids != null && ids.length > 0) {
			try {
				for (String id : ids) {
					Resource resource = resourceService.get(Resource.class,
							Integer.parseInt(id));
					if (resource != null) {
						resourceService.delete(resource);
					}
				}
				map.put("msg", "删除成功！");
				return map;
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				map.put("msg", "删除失败！");
				return map;
			}
		} else {
			map.put("msg", "删除失败！");
		}
		return map;
	}

	/**
	 * 删除单条记录
	 * 
	 * @param resource
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public ResponseEx delete(Resource resource) {
		ResponseEx ex = new ResponseEx();
		if(resource.getId() != null) {
			try {
				resourceService.delete(resource);
				ex.setSuccess("删除成功");
			} catch (Exception e) {
				ex.setFail("删除失败");
			}
			return ex;
		}
		ex.setFail("删除失败");
		return ex;
	}

	@RequestMapping("list")
	public String list(Model model) {
		List<ResourceType> list = resourceTypeService
				.find("from ResourceType r where 1 = 1");
		if (list == null || list.size() < 1) {
			// 没有数据，在页面显示新增按钮
			model.addAttribute("emptyTreeFlag", "1");
		}
		model.addAttribute("fileType", DataDictionary.getObject("file_type"));
		return "/resource/resource_list";
	}

	@ResponseBody
	@RequestMapping("/listData")
	public Grid<Resource> listData(String resourceTypeId, PageHelper page) {
		//System.out.println("resourceTypeId=" + resourceTypeId);
		List<Resource> list = null;
		ResourceType rt = null;
		if (StringUtils.isNotBlank(resourceTypeId)) {
			Integer id = Integer.parseInt(resourceTypeId);
			rt = resourceTypeService.get(ResourceType.class, id);
			//如果是根结点则查所有
			if(rt.getParent() == null) {
				list = resourceService.find("from Resource r order by r.sort desc",
						null, page);
			}
			else {
				list = resourceService
						.find("from Resource r where r.resourceType.id = ? order by r.sort desc",
								new Object[] { id }, page);
			}
		} else {
			list = resourceService.find("from Resource r order by r.sort desc",
					null, page);
		}

		return new Grid<Resource>(list);
	}
	
	/**
	 * 把所有资源分类转成json字符串
	 */
	@ResponseBody
	@RequestMapping(value = "listAll", produces = "application/json;charset=UTF-8")
	public String listAll(HttpServletResponse response) {
		List<Map<String, Object>> mapList = Lists.newArrayList();
		List<Resource> lists = resourceService
				.find("from Resource t order by t.createDate asc");
		Map<String, Object> map = Maps.newHashMap();
		String rootId = "1111111111";
		map.put("id", rootId);
		for (Resource e : lists) {
			map.put("id", e.getId());
			map.put("pid", rootId);
			String path = e.getFilePath();
			path = path.substring(path.indexOf("资源")+3, path.length());
			map.put("text", path);
			mapList.add(map);
		}
		String jsonStr = JsonMapper.toJsonString(mapList);
		return jsonStr;
	}

	/**
	 * 资源位置移动
	 * 
	 * @param srcId
	 *            待移动的资源id
	 * @param targetId
	 *            移动到的目标位置资源id
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping("move")
	@ResponseBody
	public Map<String, String> move(Integer srcId, Integer targetId,
			HttpServletResponse response, HttpServletRequest request) {
		Map<String, String> map = new HashMap<String, String>();
		if(srcId != null && targetId != null) {
			try {
				Resource r1 = resourceService.get(Resource.class, srcId);
				Resource r2 = resourceService.get(Resource.class, targetId);
				Integer sort1 = r1.getSort();
				r1.setSort(r2.getSort());
				r2.setSort(sort1);
				resourceService.update(r1);
				resourceService.update(r2);
				map.put("msg", "success");
			} catch (Exception e) {
				map.put("msg", "failure");
			}
			return map;
		}
		map.put("msg", "failure");
		return map;
	}

	/**
	 * 查看详情
	 * 
	 * @param resourceId
	 * @param model
	 * @return
	 */
	@RequestMapping("detail")
	public String detail(Resource resource, Model model) {
		if (resource.getId() != null) {
			resource = resourceService.get(Resource.class, resource.getId());
			model.addAttribute("resource", resource);
			List<ResourceType> lists = resourceTypeService
					.find("from ResourceType r where 1 = 1");
			/*
			 * Set<UserOrganization> orgs =
			 * resource.getUpdator().getUserOrganizations(); StringBuffer sb =
			 * new StringBuffer(); for(UserOrganization org: orgs) {
			 * Organization o = org.getOrganization();
			 * sb.append(o.getName()).append("、"); } if(sb.lastIndexOf("、") > 0)
			 * { model.addAttribute("orgs", sb.substring(0, sb.length() - 1 ));
			 * }
			 */
			model.addAttribute("fileType", DataDictionary.getObject("file_type"));
			model.addAttribute("lists", lists);
			model.addAttribute("clusterNames", ClusterUtils.lists());
		}
		return "/resource/resource_view";
	}

	
	@ResponseBody
	@RequestMapping("getResources")
	public List<Resource> getResources(String resourceTypeId,
			HttpServletResponse response) {
		resourceService.find(
				"select r from Resource r where r.resourceType.id = ?",
				new Object[] { resourceTypeId });
		return null;
	}

}