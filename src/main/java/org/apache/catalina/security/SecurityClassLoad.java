package org.apache.catalina.security;

/**
 * Created by tianye on 2018/9/4.
 */
public class SecurityClassLoad {
    public static void securityClassLoad(ClassLoader loader)
            throws Exception {

        /**
         * java安全管理器_securityManager
         * java安全应该包括两个方面的内容: 一是java平台(即java运行环境)的安全性;二是java语言开发的应用程序的安全性。
         * java平台的安全性由虚拟机维护,而第二个安全性则需要自己维护。一般可以通过安全管理器机制来完善安全性,
         * 安全管理器是安全的实施者,可对此类进行扩展。
         * 通过配置安全策略文件达到对网络、本地文件、程序其他部分的访问限制的效果
         *
         * 本质上是对访问权限的控制
         *
         * 启动安全管理器:
         * 2、 启动安全管理器:
         java程序启动时,默认并不会启动安全管理器,一般有两种方法启动安全管理器:
         a. 一种是隐式的: 启动默认的安全管理器的最简单的方法是: 直接在启动命令中添加 -Djava.security.manager参数即可。可以通过System.getProperty("java.security.manager")检查该系统属性是否被设置

         b. 一种是显式的: 实例化一个java.lang.SecurityManager或继承他的子类的对象,然后通过System.setSecurityManger()来设置并启动一个安全管理器
         打开:
         SecurityManager sm = new SecurityManager();
         SecurityManager securityManager = System.getSecurityManager();
         System.setSecurityManager(sm);

         关闭:
         SecurityManager securityManager = System.getSecurityManager();
         if ( sm != null ) {
         System.setSecurityManager(null);
         }
         注:

         1). 在启动安全管理器时,可以通过-Djava.security.policy选项来指定安全策略文件。
         如果没有指定策略文件的路径,默认的安全策略文件为 %JAVA_HOME%/jre/lib/security/java.policy。
         2). "="表示这个策略文件将和默认的策略文件一同发挥作用; "=="表示只使用指定的策略文件 。
         如 -Djava.security.policy==E:/temp/test1.policy
         或者 -Djava.security.policy=bin/com/test/test1.policy

         java中的权限类别:

         java.security.AllPermission --所有权限的集合
         java.util.PropertyPermission --系统/环境属性权限
         java.lang.RuntimePermission --运行时权限
         java.net.SocketPermission --Socket权限
         java.io.FilePermission --文件权限,包括读写,删除,执行
         java.io.SerializablePermission --序列化权限
         java.lang.reflect.ReflectPermission --反射权限
         java.security.UnresolvedPermission --未解析的权限
         java.net.NetPermission --网络权限
         java.awt.AWTPermission --AWT权限
         java.sql.SQLPermission --数据库sql权限
         java.security.SecurityPermission --安全控制方面的权限
         java.util.logging.LoggingPermission --日志控制权限
         javax.net.ssl.SSLPermission --安全连接权限
         javax.security.auth.AuthPermission --认证权限
         javax.sound.sampled.AudioPermission --音频系统资源的访问权限
         */
        if( System.getSecurityManager() == null ){
            return;
        }

        /**
         *
         * 每个Java应用都可以有自己的安全管理器，它是防范恶意攻击的主要安全卫士。
         * 安全管理器通过执行运行阶段检查和访问授权，以实施应用所需的安全策略，从而保护资源免受恶意操作的攻击。
         * 实际上，安全管理器根据Java安全策略文件决定将哪组权限授予类。
         * 然而，当不可信的类和第三方应用使用JVM时，
         * Java安全管理器将使用与JVM相关的安全策略来识别恶意操作。
         * 在很多情况下，威胁模型不包含运行于JVM中的恶意代码，
         * 此时Java安全管理器便不是必需的。当安全管理器检测到违反安全策略的操作时，
         * JVM将引发AccessControlException或SecurityException。
         *
         *
         *      在Java应用中，安全管理器是由System类中的方法setSecurityManager设置的。
         *      要获得当前的安全管理器，可以使用方法getSecurityManager。
         *
         *
         java.lang.SecurityManager类包含了很多checkXXXX方法，如用于判断对文件访问权限的checkRead(String
         file)方法。这些检查方法调用SecurityManager.checkPermission方法，
         后者根据安全策略文件判断调用应用是否有执行所请求的操作权限。如果没有，将引发SecurityException。



         如果想让应用使用安全管理器和安全策略，可在启动JVM时设定-Djava.security.manager选项，还可以同时指定安全策略文件。
         如果在应用中启用了Java安全管理器，却没有指定安全策略文件，那么Java安全管理器将使用默认的安全策略，
         它们是由位于目录$JAVA_HOME/jre/lib/security中的java.policy定义的。


         概念
         策略(Policy)
             类装载器用Policy对象帮助它们决定，把一段代码导入虚拟机时应该给它们什么样的权限.
         任何时候，每一个应用程序都只有一个Policy对象.

         策略文件
             Sun的java1.2平台具体的Policy子类采用在一ASCII策略文件中用上下文无关文法描述安全策略.
             一个策略文件包括了一系列grant子句，每一个grant子句将一些权限授给一个代码来源。

         保护域(ProtectionDomain)
         当类装载器将(((类型)))装入java虚拟机时，它们将为每一个(((类型)))指派一个保护域，保护域定义了授予
         一段特定的代码的所有权限.装载入java虚拟机的每一个类型都属于一个且仅属于一个保护域.

         访问控制器(AccessController)

              implies()
                   判断一个Permissioin对象的权限，是否隐含(imply)在另一个Permissioin对象的权限中。

             checkPermission()
                  AccessController的核心方法，这个方法决定一个特定的操作能否被允许.
                  它自顶向下检查栈，只要它遇到一个没有权限桢，它将抛出一个AccessControlException导常。

             doPrivileged()
                  有的时候，调用栈较上层(更靠近栈顶)的代码可能希望执行一段代码，而这段代码在调用栈的较
                  下层是不允许执行的。
                   为了使可信的代码执行较不可靠的代码操作(这段不可靠的代码位于调用栈的较下层且没有执行
                   这个操作的权限),AccessController类重载了四个名为doPrivileged()的静态方法.
                   AccessController会忽略调用doPrivileged()方法的调用者的调用者的权限.

             Permission:
                   权限是用抽象类java.security.Permission的一个子类的实例表示的.

             CodeSource:
                  代码来源，包含代码库URL和签名者.

             Permissions:
                 PermissionCollection(权限集合)的子类

         装载时生成保护域的步骤:
         1           根据指定的Policy文件生成一个Policy对象
         2           生成CodeSource代码源
         3           用CodeSource在Policy中找到CodeSource对应的Permissions
         4           用CodeSource和Permissons构造一个ProtectionDomain
         5           把ProtectionDomain同这个类在方法区中的类数据联系起来(ClassLoader.defineClass()).

         https://blog.csdn.net/mra__s__/article/details/60764051   重要非常
         https://blog.csdn.net/expleeve/article/details/54016370
         https://www.2cto.com/kf/201803/732004.html
         https://blog.csdn.net/qq_18377515/article/details/79591660
**/

        loadCorePackage(loader);
        loadCoyotePackage(loader);
        loadLoaderPackage(loader);
        loadRealmPackage(loader);
        loadServletsPackage(loader);
        loadSessionPackage(loader);
        loadUtilPackage(loader);
        loadValvesPackage(loader);
        loadJavaxPackage(loader);
        loadConnectorPackage(loader);
        loadTomcatPackage(loader);
    }


    private static final void loadCorePackage(ClassLoader loader)
            throws Exception {
        final String basePackage = "org.apache.catalina.core.";
        loader.loadClass
                (basePackage +
                        "AccessLogAdapter");
        loader.loadClass
                (basePackage +
                        "ApplicationContextFacade$1");
        loader.loadClass
                (basePackage +
                        "ApplicationDispatcher$PrivilegedForward");
        loader.loadClass
                (basePackage +
                        "ApplicationDispatcher$PrivilegedInclude");
        loader.loadClass
                (basePackage +
                        "AsyncContextImpl");
        loader.loadClass
                (basePackage +
                        "AsyncContextImpl$DebugException");
        loader.loadClass
                (basePackage +
                        "AsyncContextImpl$1");
        loader.loadClass
                (basePackage +
                        "AsyncListenerWrapper");
        loader.loadClass
                (basePackage +
                        "ContainerBase$PrivilegedAddChild");
        loader.loadClass
                (basePackage +
                        "DefaultInstanceManager$1");
        loader.loadClass
                (basePackage +
                        "DefaultInstanceManager$2");
        loader.loadClass
                (basePackage +
                        "DefaultInstanceManager$3");
        loader.loadClass
                (basePackage +
                        "DefaultInstanceManager$AnnotationCacheEntry");
        loader.loadClass
                (basePackage +
                        "DefaultInstanceManager$AnnotationCacheEntryType");
        loader.loadClass
                (basePackage +
                        "ApplicationHttpRequest$AttributeNamesEnumerator");
    }


    private static final void loadLoaderPackage(ClassLoader loader)
            throws Exception {
        final String basePackage = "org.apache.catalina.loader.";
        loader.loadClass
                (basePackage +
                        "ResourceEntry");
        loader.loadClass
                (basePackage +
                        "WebappClassLoaderBase$PrivilegedFindResourceByName");
    }


    private static final void loadRealmPackage(ClassLoader loader)
            throws Exception {
        final String basePackage = "org.apache.catalina.realm.";
        loader.loadClass
                (basePackage + "LockOutRealm$LockRecord");
    }


    private static final void loadServletsPackage(ClassLoader loader)
            throws Exception {
        final String basePackage = "org.apache.catalina.servlets.";
        // Avoid a possible memory leak in the DefaultServlet when running with
        // a security manager. The DefaultServlet needs to load an XML parser
        // when running under a security manager. We want this to be loaded by
        // the container rather than a web application to prevent a memory leak
        // via web application class loader.
        loader.loadClass(basePackage + "DefaultServlet");
    }


    private static final void loadSessionPackage(ClassLoader loader)
            throws Exception {
        final String basePackage = "org.apache.catalina.session.";
        loader.loadClass
                (basePackage + "StandardSession");
        loader.loadClass
                (basePackage + "StandardSession$1");
        loader.loadClass
                (basePackage + "StandardManager$PrivilegedDoUnload");
    }


    private static final void loadUtilPackage(ClassLoader loader)
            throws Exception {
        final String basePackage = "org.apache.catalina.util.";
        loader.loadClass(basePackage + "Enumerator");
        loader.loadClass(basePackage + "ParameterMap");
        loader.loadClass(basePackage + "RequestUtil");
    }


    private static final void loadValvesPackage(ClassLoader loader)
            throws Exception {
        final String basePackage = "org.apache.catalina.valves.";
        loader.loadClass(basePackage + "AccessLogValve$3");
    }


    private static final void loadCoyotePackage(ClassLoader loader)
            throws Exception {
        final String basePackage = "org.apache.coyote.";
        loader.loadClass(basePackage + "http11.AbstractOutputBuffer$1");
        loader.loadClass(basePackage + "http11.Constants");
        // Make sure system property is read at this point
        Class<?> clazz = loader.loadClass(basePackage + "Constants");
        clazz.newInstance();
    }


    private static final void loadJavaxPackage(ClassLoader loader)
            throws Exception {
        loader.loadClass("javax.servlet.http.Cookie");
    }


    private static final void loadConnectorPackage(ClassLoader loader)
            throws Exception {
        final String basePackage = "org.apache.catalina.connector.";
        loader.loadClass
                (basePackage +
                        "RequestFacade$GetAttributePrivilegedAction");
        loader.loadClass
                (basePackage +
                        "RequestFacade$GetParameterMapPrivilegedAction");
        loader.loadClass
                (basePackage +
                        "RequestFacade$GetRequestDispatcherPrivilegedAction");
        loader.loadClass
                (basePackage +
                        "RequestFacade$GetParameterPrivilegedAction");
        loader.loadClass
                (basePackage +
                        "RequestFacade$GetParameterNamesPrivilegedAction");
        loader.loadClass
                (basePackage +
                        "RequestFacade$GetParameterValuePrivilegedAction");
        loader.loadClass
                (basePackage +
                        "RequestFacade$GetCharacterEncodingPrivilegedAction");
        loader.loadClass
                (basePackage +
                        "RequestFacade$GetHeadersPrivilegedAction");
        loader.loadClass
                (basePackage +
                        "RequestFacade$GetHeaderNamesPrivilegedAction");
        loader.loadClass
                (basePackage +
                        "RequestFacade$GetCookiesPrivilegedAction");
        loader.loadClass
                (basePackage +
                        "RequestFacade$GetLocalePrivilegedAction");
        loader.loadClass
                (basePackage +
                        "RequestFacade$GetLocalesPrivilegedAction");
        loader.loadClass
                (basePackage +
                        "ResponseFacade$SetContentTypePrivilegedAction");
        loader.loadClass
                (basePackage +
                        "ResponseFacade$DateHeaderPrivilegedAction");
        loader.loadClass
                (basePackage +
                        "RequestFacade$GetSessionPrivilegedAction");
        loader.loadClass
                (basePackage +
                        "ResponseFacade$1");
        loader.loadClass
                (basePackage +
                        "OutputBuffer$1");
        loader.loadClass
                (basePackage +
                        "CoyoteInputStream$1");
        loader.loadClass
                (basePackage +
                        "CoyoteInputStream$2");
        loader.loadClass
                (basePackage +
                        "CoyoteInputStream$3");
        loader.loadClass
                (basePackage +
                        "CoyoteInputStream$4");
        loader.loadClass
                (basePackage +
                        "CoyoteInputStream$5");
        loader.loadClass
                (basePackage +
                        "InputBuffer$1");
        loader.loadClass
                (basePackage +
                        "Response$1");
        loader.loadClass
                (basePackage +
                        "Response$2");
        loader.loadClass
                (basePackage +
                        "Response$3");
    }

    private static final void loadTomcatPackage(ClassLoader loader)
            throws Exception {
        final String basePackage = "org.apache.tomcat.";
        // buf
        loader.loadClass(basePackage + "util.buf.HexUtils");
        loader.loadClass(basePackage + "util.buf.StringCache");
        loader.loadClass(basePackage + "util.buf.StringCache$ByteEntry");
        loader.loadClass(basePackage + "util.buf.StringCache$CharEntry");
        loader.loadClass(basePackage + "util.buf.UriUtil");
        // http
        loader.loadClass(basePackage + "util.http.HttpMessages");
        // Make sure system property is read at this point
        Class<?> clazz = loader.loadClass(
                basePackage + "util.http.FastHttpDateFormat");
        clazz.newInstance();
        loader.loadClass(basePackage + "util.http.HttpMessages");
        loader.loadClass(basePackage + "util.http.parser.HttpParser");
        loader.loadClass(basePackage + "util.http.parser.HttpParser$SkipConstantResult");
        loader.loadClass(basePackage + "util.http.parser.MediaType");
        loader.loadClass(basePackage + "util.http.parser.MediaTypeCache");
        // net
        loader.loadClass(basePackage + "util.net.Constants");
        loader.loadClass(basePackage +
                "util.net.NioBlockingSelector$BlockPoller$1");
        loader.loadClass(basePackage +
                "util.net.NioBlockingSelector$BlockPoller$2");
        loader.loadClass(basePackage +
                "util.net.NioBlockingSelector$BlockPoller$3");
        loader.loadClass(basePackage + "util.net.SSLSupport$CipherData");
        // security
        loader.loadClass(basePackage + "util.security.PrivilegedGetTccl");
        loader.loadClass(basePackage + "util.security.PrivilegedSetTccl");
    }
}
