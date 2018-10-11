/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.catalina;


import java.util.EventObject;

/**
 * 这是事件类，继承java的事件类EventObject，咱们自己写的时候，按着这么写
 */
public final class LifecycleEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    /**
     *
     * @param lifecycle 触发该事件的具体实例对象
     * @param type 事件类型（对应于Lifecycle中定义的几种状态）
     * @param data 该事件携带的参数数据
     */
    public LifecycleEvent(Lifecycle lifecycle, String type, Object data) {

        super(lifecycle);
        this.type = type;
        this.data = data;
    }

    private Object data = null;

    private String type = null;

    public Object getData() {

        return (this.data);

    }

    public Lifecycle getLifecycle() {

        return (Lifecycle) getSource();

    }

    public String getType() {

        return (this.type);

    }


}
