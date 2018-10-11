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

import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.startup.Catalina;


public interface Server extends Lifecycle {

    public String getInfo();

    public NamingResources getGlobalNamingResources();

    public void setGlobalNamingResources(NamingResources globalNamingResources);

    public javax.naming.Context getGlobalNamingContext();

    public int getPort();

    public void setPort(int port);

    public String getAddress();

    public void setAddress(String address);

    public String getShutdown();

    public void setShutdown(String shutdown);

    public ClassLoader getParentClassLoader();


    public void setParentClassLoader(ClassLoader parent);


    public Catalina getCatalina();

    public void setCatalina(Catalina catalina);

    public void addService(Service service);

    public void await();

    public Service findService(String name);

    public Service[] findServices();

    public void removeService(Service service);
}
