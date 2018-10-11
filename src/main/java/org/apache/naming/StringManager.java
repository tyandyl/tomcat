/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.naming;

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class StringManager {

    /**
     * 这个我怎么觉得和spring的MessageSource 功能类似
     * 这个类主要用来解决国际化和本地化问题
     *
     * 比如对于“取消”，中文中我们使用“取消”来表示，而英文中我们使用“cancel”。
     * 若我们的程序是面向国际的（这也是软件发展的一个趋势），那么使用的人群必然是多语言环境的，
     * 实现国际化就非常有必要。而ResourceBundle可以帮助我们轻松完成这个任务：
     * 当程序需要一个特定于语言环境的资源时（如 String），程序可以从适合当前用户语言环境的资源包
     * （大多数情况下也就是.properties文件）中加载它。这样可以编写很大程度上独立于用户语言环境的程序代码，
     * 它将资源包中大部分（即便不是全部）特定于语言环境的信息隔离开来。

     * 这个类的作用就是读取资源属性文件（properties），然后根据.properties文件的名称信息（本地化信息），匹配当前系统的国别语言信息（也可以程序指定），然后获取相应的properties文件的内容。

     使用这个类，properties需要遵循一定的命名规范，
     一般的命名规范是： 自定义名语言代码国别代码.properties，如果是默认的，直接写为：自定义名.properties。
     比如：

     myres_en_US.properties
     myres_zh_CN.properties

     myres.properties

     当在中文操作系统下，如果myres_zh_CN.properties、myres.properties两个文件都存在，则优先会使用myres_zh_CN.properties，
     当myres_zh_CN.properties不存在时候，会使用默认的myres.properties。

     没有提供语言和地区的资源文件是系统默认的资源文件。

     资源文件都必须是ISO-8859-1编码，因此，对于所有非西方语系的处理，都必须先将之转换为Java Unicode Escape格式。
     转换方法是通过JDK自带的工具native2ascii.

     测试及验证
     1.新建4个属性文件：
     my_en_US.properties：cancelKey=cancel

     my_zh_CN.properties：cancelKey=\u53D6\u6D88（取消）

     my_zh.properties：cancelKey=\u53D6\u6D88zh（取消zh）

     my.properties：cancelKey=\u53D6\u6D88default（取消default）

     2.获取bundle
     ResourceBundle bundle = ResourceBundle.getBundle("my", new Locale("zh", "CN"));

     其中new Locale(“zh”, “CN”)提供本地化信息，上面这行代码，
     程序会首先在classpath下寻找my_zh_CN.properties文件，若my_zh_CN.properties文件不存在，
     则取找my_zh.properties，如还是不存在，继续寻找my.properties,若都找不到就抛出异常。


     */
    private final ResourceBundle bundle;

    /**
     * 创建一个通用英语的locale.
     * Locale locale11 = new Locale("en");
     * 创建一个加拿大英语的locale.
     * Locale locale12 = new Locale("en", "CA");
     */
    private final Locale locale;

    /**
     * Creates a new StringManager for a given package. This is a
     * private method and all access to it is arbitrated by the
     * static getManager method call so that only one StringManager
     * per package will be created.
     *
     * @param packageName Name of package to create StringManager for.
     */
    private StringManager(String packageName) {
        String bundleName = packageName + ".LocalStrings";
        ResourceBundle tempBundle = null;
        try {
            tempBundle = ResourceBundle.getBundle(bundleName, Locale.getDefault());
        } catch( MissingResourceException ex ) {
            // Try from the current loader (that's the case for trusted apps)
            // Should only be required if using a TC5 style classloader structure
            // where common != shared != server
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if( cl != null ) {
                try {
                    tempBundle = ResourceBundle.getBundle(
                            bundleName, Locale.getDefault(), cl);
                } catch(MissingResourceException ex2) {
                    // Ignore
                }
            }
        }
        // Get the actual locale, which may be different from the requested one
        if (tempBundle != null) {
            locale = tempBundle.getLocale();
        } else {
            locale = null;
        }
        bundle = tempBundle;
    }

    /**
     Get a string from the underlying resource bundle or return
     null if the String is not found.

     @param key to desired resource String
     @return resource String matching <i>key</i> from underlying
     bundle or null if not found.
     @throws IllegalArgumentException if <i>key</i> is null.
     */
    public String getString(String key) {
        if(key == null){
            String msg = "key may not have a null value";

            throw new IllegalArgumentException(msg);
        }

        String str = null;

        try {
            str = bundle.getString(key);
        } catch(MissingResourceException mre) {
            //bad: shouldn't mask an exception the following way:
            //   str = "[cannot find message associated with key '" + key + "' due to " + mre + "]";
            //     because it hides the fact that the String was missing
            //     from the calling code.
            //good: could just throw the exception (or wrap it in another)
            //      but that would probably cause much havoc on existing
            //      code.
            //better: consistent with container pattern to
            //      simply return null.  Calling code can then do
            //      a null check.
            str = null;
        }

        return str;
    }

    /**
     * Get a string from the underlying resource bundle and format
     * it with the given set of arguments.
     *
     * @param key  The key for the required message
     * @param args The values to insert into the message
     *
     * @return The request string formatted with the provided arguments or the
     *         key if the key was not found.
     */
    public String getString(final String key, final Object... args) {
        String value = getString(key);
        if (value == null) {
            value = key;
        }

        MessageFormat mf = new MessageFormat(value);
        mf.setLocale(locale);
        return mf.format(args, new StringBuffer(), null).toString();
    }

    // --------------------------------------------------------------
    // STATIC SUPPORT METHODS
    // --------------------------------------------------------------

    private static final Hashtable<String, StringManager> managers =
            new Hashtable<String, StringManager>();

    /**
     * Get the StringManager for a particular package. If a manager for
     * a package already exists, it will be reused, else a new
     * StringManager will be created and returned.
     *
     * @param packageName The package name
     *
     * @return The instance associated with the given package
     */
    public static final synchronized StringManager getManager(String packageName) {
        StringManager mgr = managers.get(packageName);
        if (mgr == null) {
            mgr = new StringManager(packageName);
            managers.put(packageName, mgr);
        }
        return mgr;
    }


    public static final StringManager getManager(Class<?> clazz) {
        return getManager(clazz.getPackage().getName());
    }
}
