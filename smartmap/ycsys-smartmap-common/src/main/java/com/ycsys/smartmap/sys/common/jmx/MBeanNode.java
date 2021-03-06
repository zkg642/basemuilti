/*
 * Copyright 2008-2016 by Emeric Vernat
 *
 *     This file is part of Java Melody.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ycsys.smartmap.sys.common.jmx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * MBeanNode
 * @author lixiaoxin
 */
public class MBeanNode implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String name;

	private final String description;

	private final List<com.ycsys.smartmap.sys.common.jmx.MBeanNode> children;

	private final List<MBeanAttribute> attributes;

	static class MBeanAttribute implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String name;

		private final String description;

		private final String formattedValue;

		MBeanAttribute(String name, String description, String formattedValue) {
			super();
			this.name = name;
			this.description = description;
			this.formattedValue = formattedValue;
		}

		String getName() {
			return name;
		}

		String getDescription() {
			return description;
		}

		String getFormattedValue() {
			return formattedValue;
		}

		/** {@inheritDoc} */
		@Override
		public String toString() {
			return getClass().getSimpleName() + "[name=" + getName() + ", formattedValue="
					+ getFormattedValue() + ']';
		}
	}

	MBeanNode(String name) {
		super();
		this.name = name;
		this.description = null;
		this.children = new ArrayList<com.ycsys.smartmap.sys.common.jmx.MBeanNode>();
		this.attributes = null;
	}

	MBeanNode(String name, String description, List<MBeanAttribute> attributes) {
		super();
		this.name = name;
		this.description = description;
		this.children = null;
		this.attributes = attributes;
	}

	String getName() {
		return name;
	}

	String getDescription() {
		return description;
	}

	List<com.ycsys.smartmap.sys.common.jmx.MBeanNode> getChildren() {
		return children != null ? children : null;
	}

	List<MBeanAttribute> getAttributes() {
		return attributes != null ? attributes : null;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=" + getName() + ']';
	}
}
