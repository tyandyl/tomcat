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


/**
 * 由于很多对象类型都是具有生命周期的，如果每个需要生命周期的类都去实现一个Lifecycle接口实现里面的逻辑将
 * 会出现很多冗余代码， 有一种方式可以解决，就是写一个基类实现Lifecycle接口，然后其他类都基础该基类，
 * 但是由于java单继承限制， 感觉用一个Lifecycle的实现类去作为一个父类不太合适，太局限了，
 * 好比让“人类”继承“跑步”父类一样。
 所以在tomcat里面用了一种设计模式，我也不太清楚这种设计模式叫什么名字， 先姑且叫它“伪继承组合”模式，
 使用一个LifecycleSupport类来管理一个Lifecycle的具体实例，并且有同名方法addLifecycleListener与
 getLifecycleListener，以及一个fireLifecycleListener方法来触发lifecycle的所有监听器
 尼玛，这不是代理模式么
 public class Engine implements Lifecycle{
    private LifecycleSupport lifecycle = new LifecycleSupport(this);
 }
 */
public interface Lifecycle {

    public static final String BEFORE_INIT_EVENT = "before_init";

    public static final String AFTER_INIT_EVENT = "after_init";

    public static final String START_EVENT = "start";

    public static final String BEFORE_START_EVENT = "before_start";

    public static final String AFTER_START_EVENT = "after_start";

    public static final String STOP_EVENT = "stop";

    public static final String BEFORE_STOP_EVENT = "before_stop";

    public static final String AFTER_STOP_EVENT = "after_stop";

    public static final String AFTER_DESTROY_EVENT = "after_destroy";

    public static final String BEFORE_DESTROY_EVENT = "before_destroy";

    public static final String PERIODIC_EVENT = "periodic";

    public static final String CONFIGURE_START_EVENT = "configure_start";

    public static final String CONFIGURE_STOP_EVENT = "configure_stop";

    public void addLifecycleListener(LifecycleListener listener);

    public LifecycleListener[] findLifecycleListeners();

    public void removeLifecycleListener(LifecycleListener listener);

    public void init() throws LifecycleException;

    public void start() throws LifecycleException;


    public void stop() throws LifecycleException;

    public void destroy() throws LifecycleException;

    public LifecycleState getState();


    public String getStateName();


    public interface SingleUse {
    }
}
