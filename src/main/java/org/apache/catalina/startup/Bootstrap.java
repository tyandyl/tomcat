package org.apache.catalina.startup;

import org.apache.catalina.Globals;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.catalina.security.SecurityClassLoad;
import org.apache.catalina.startup.ClassLoaderFactory.Repository;
import org.apache.catalina.startup.ClassLoaderFactory.RepositoryType;

/**
 * Created by tianye on 2018/9/4.
 */
public class Bootstrap {
    //首先整个Tomcat的classLoader分为了两条线，左边的一条线为catalinaLoader，
    // 这个是Tomcat服务器专用的，用于加载Tomcat服务器本身的class，右边的一条线则为web应用程序用的，
    // 每一个web应用程序都有自己专用的WebappClassLoader，用于加载属于自己应用程序的资源，
    // 例如/web-inf/lib下面的jar包，classes里面的class文件。。。

    /**
     * 这个是父类加载器，因为根据双亲构建法，构建一个子加载器，需要一个父类加载器
     * 这个加载器本质上就是main函数所在类的加载器
     */
    ClassLoader commonLoader = null;
    ClassLoader catalinaLoader = null;
    ClassLoader sharedLoader = null;

    private static Bootstrap daemon = null;

    private Object catalinaDaemon = null;

    /**
     * 这个是tomcat的main函数，tomcat就是在这里开始的
     * @param args
     */
    public static void main(String args[]) {

        // 创建一个 Bootstrap 实例
        if (daemon == null) {
            // Don't set daemon until init() has completed
            Bootstrap bootstrap = new Bootstrap();
            try {

                // 初始化ClassLoader、并用ClassLoader创建了 Catalina 实例，赋给了 catalinaDaemon 变量
                bootstrap.init();
            } catch (Throwable t) {
                handleThrowable(t);
                t.printStackTrace();
                return;
            }
            daemon = bootstrap;
        } else {
            // When running as a service the call to stop will be on a new
            // thread so make sure the correct class loader is used to prevent
            // a range of class not found exceptions.
            Thread.currentThread().setContextClassLoader(daemon.catalinaLoader);
        }

        try {
            String command = "start";
            if (args.length > 0) {
                command = args[args.length - 1];
            }

            if (command.equals("startd")) {
                args[args.length - 1] = "start";
                daemon.load(args);
                daemon.start();
            } else if (command.equals("stopd")) {
                args[args.length - 1] = "stop";
                daemon.stop();
            } else if (command.equals("start")) {
                daemon.setAwait(true);
                daemon.load(args);
                daemon.start();
            } else if (command.equals("stop")) {
                daemon.stopServer(args);
            } else if (command.equals("configtest")) {
                daemon.load(args);
                if (null==daemon.getServer()) {
                    System.exit(1);
                }
                System.exit(0);
            } else {
                System.out.println("Bootstrap: command \"" + command + "\" does not exist.");
            }
        } catch (Throwable t) {
            // Unwrap the Exception for clearer error reporting
            if (t instanceof InvocationTargetException &&
                    t.getCause() != null) {
                t = t.getCause();
            }
            handleThrowable(t);
            t.printStackTrace();
            System.exit(1);
        }

    }






    public void init()
            throws Exception
    {

        //构建tomcat安装目录，CATALINA_HOME是Tomcat的安装目录，
        // CATALINA_BASE是Tomcat的工作目录。如果我们想要运行Tomcat的 多个实例，
        // 但是不想安装多个Tomcat软件副本。那么我们可以配置多个工作 目录，
        // 每个运行实例独占一个工作目录，但是共享同一个安装目录。
        // Tomcat每个运行实例需要使用自己的conf、logs、temp、webapps、work和shared目录，
        // 因此CATALINA_BASE就 指向这些目录。 而其他目录主要包括了Tomcat的二进制文件和脚本，
        // CATALINA_HOME就指向这些目录。如果我们希望再运行另一个Tomcat实例，那么我们可以建立一个目录，
        // 把conf、logs、temp、webapps、work和shared拷贝 到该目录下，然后让CATALINA_BASE指向该目录即可
        setCatalinaHome();
        setCatalinaBase();

       //初始化 classLoader
        initClassLoaders();

        //这里我们将当前加载器设置成tomcat服务器专用的加载器
        Thread.currentThread().setContextClassLoader(catalinaLoader);

        //SecurityClassLoad.securityClassLoad(catalinaLoader)的作用就是线程安全的加载class
        /**
         * securityClassLoad方法主要负责加载Tomcat容器所需的class，包括：

         Tomcat核心class，即org.apache.catalina.core路径下的class；
         org.apache.catalina.loader.WebappClassLoader$PrivilegedFindResourceByName；
         Tomcat有关session的class，即org.apache.catalina.session路径下的class；
         Tomcat工具类的class，即org.apache.catalina.util路径下的class；
         javax.servlet.http.Cookie；
         Tomcat处理请求的class，即org.apache.catalina.connector路径下的class；
         Tomcat其它工具类的class，也是org.apache.catalina.util路径下的class；

         */
        SecurityClassLoad.securityClassLoad(catalinaLoader);


        //加载 org.apache.catalina.startup.Catalina class
        Class<?> startupClass =
                catalinaLoader.loadClass
                        ("org.apache.catalina.startup.Catalina");

        // 实例化 Catalina 实例
        Object startupInstance = startupClass.newInstance();


        String methodName = "setParentClassLoader";
        Class<?> paramTypes[] = new Class[1];
        paramTypes[0] = Class.forName("java.lang.ClassLoader");
        Object paramValues[] = new Object[1];
        paramValues[0] = sharedLoader;
        Method method =
                startupInstance.getClass().getMethod(methodName, paramTypes);
        method.invoke(startupInstance, paramValues);

//        Catalina 实例赋给了 catalinaDaemon 对象
        catalinaDaemon = startupInstance;

    }

    private void initClassLoaders() {
        try {
            //首先创建commonLoader，这个加载器是tomcat服务器专用的类和appweb专用的类都可以加载
            //第一次加载的时候，在java环境中找common.loader 是没有的，所以，返回null
            commonLoader = createClassLoader("common", null);
            if( commonLoader == null ) {
                //因为返回null，所以使用当前类的加载器
                commonLoader=this.getClass().getClassLoader();
            }
            //第一次加载的时候，在java环境中找server.loader 是没有的，所以，这里使用commonLoader加载器
            catalinaLoader = createClassLoader("server", commonLoader);
            //第一次加载的时候，在java环境中找shared.loader 是没有的，所以，这里使用commonLoader加载器
            sharedLoader = createClassLoader("shared", commonLoader);
        } catch (Throwable t) {
            handleThrowable(t);
            System.exit(1);
        }
    }

    private ClassLoader createClassLoader(String name, ClassLoader parent)
            throws Exception {

        //查询%CATALINA_HOME%/conf/catalina.properties 中的common.loader和server.loader和shared.loader
        /**
         * common.loader 的value为：（
           1）${catalina.base}/lib主要在下面这些路径中加载：
         （2）${catalina.base}/lib/*.jar
         （3）${catalina.home}/lib
         （4）${catalina.home}/lib/*.jar
         在common.loader 加载完后，tomcat启动程序会检查 catalina.properties文件中配置的server.loader和shared.loader是否设置。
         如果设置，读取 tomcat下对应的server和shared这两个目录的类库。
         server和shared是对应tomcat目录下的两个目录，在Tomcat7中默认这两个目录是没有的。设置方法如下
         server.loader=${catalina.base}/server/classes,${catalina.base}/server/lib/*.jar
         shared.loader=${catalina.base}/server/classes,${catalina.base}/server/lib/*.jar
         Tomcat可以通过catalina.properties的server和shared设置，为webapp提供公用类库。
         使一些公用的、不需要与webapp放在一起的设置信息单独保存，在更新webapp的war的时候无需更改webapp的设置。
         */
        String value = CatalinaProperties.getProperty(name + ".loader");
        if ((value == null) || (value.equals("")))
            return parent;

        value = replace(value);

        List<Repository> repositories = new ArrayList<Repository>();

        StringTokenizer tokenizer = new StringTokenizer(value, ",");
        /*
        	 * G:\work eclipse workspace\tomcat8/lib
				G:\work eclipse workspace\tomcat8/lib/*.jar
				G:\work eclipse workspace\tomcat8/lib
				G:\work eclipse workspace\tomcat8/lib/*.jar
        	 */
        while (tokenizer.hasMoreElements()) {
            String repository = tokenizer.nextToken().trim();
            if (repository.length() == 0) {
                continue;
            }

            // Check for a JAR URL repository
            try {
                //我觉得这个url的意思就是，如果成功，那就是url,可以用
                @SuppressWarnings("unused")
                URL url = new URL(repository);
                repositories.add(
                        new Repository(repository, RepositoryType.URL));
                continue;
            } catch (MalformedURLException e) {
                // Ignore
            }

            // Local repository
            if (repository.endsWith("*.jar")) {
                repository = repository.substring
                        (0, repository.length() - "*.jar".length());
                repositories.add(
                        new Repository(repository, RepositoryType.GLOB));
            } else if (repository.endsWith(".jar")) {
                repositories.add(
                        new Repository(repository, RepositoryType.JAR));
            } else {
                repositories.add(
                        new Repository(repository, RepositoryType.DIR));
            }
        }

        return ClassLoaderFactory.createClassLoader(repositories, parent);
    }

    private void load(String[] arguments)
            throws Exception {

        // Call the load() method
        String methodName = "load";
        Object param[];
        Class<?> paramTypes[];
        if (arguments==null || arguments.length==0) {
            paramTypes = null;
            param = null;
        } else {
            paramTypes = new Class[1];
            paramTypes[0] = arguments.getClass();
            param = new Object[1];
            param[0] = arguments;
        }

        //catalinaDaemon  对象为 Catalina  实例  执行的为 catalina 的 stop 方法
        Method method =
                catalinaDaemon.getClass().getMethod(methodName, paramTypes);

        method.invoke(catalinaDaemon, param);

    }


    private static void handleThrowable(Throwable t) {
        if (t instanceof ThreadDeath) {
            throw (ThreadDeath) t;
        }
        if (t instanceof VirtualMachineError) {
            throw (VirtualMachineError) t;
        }
        // All other instances of Throwable will be silently swallowed
    }

    public void start()
            throws Exception {
        if( catalinaDaemon==null ) init();

        //catalinaDaemon  对象为 Catalina  实例
        Method method = catalinaDaemon.getClass().getMethod("start", (Class [] )null);

        //执行的为 catalina 的 start方法
        method.invoke(catalinaDaemon, (Object [])null);

    }

    public void stop()
            throws Exception {

        //catalinaDaemon  对象为 Catalina  实例  执行的为 catalina 的 stop 方法
        Method method = catalinaDaemon.getClass().getMethod("stop", (Class [] ) null);
        method.invoke(catalinaDaemon, (Object [] ) null);

    }

    public void setAwait(boolean await)
            throws Exception {

        Class<?> paramTypes[] = new Class[1];
        paramTypes[0] = Boolean.TYPE;
        Object paramValues[] = new Object[1];
        paramValues[0] = Boolean.valueOf(await);

        //catalinaDaemon  对象为 Catalina  实例  执行的为 catalina 的 stop 方法
        Method method =
                catalinaDaemon.getClass().getMethod("setAwait", paramTypes);
        method.invoke(catalinaDaemon, paramValues);

    }

    public boolean getAwait()
            throws Exception
    {
        Class<?> paramTypes[] = new Class[0];
        Object paramValues[] = new Object[0];

        //catalinaDaemon  对象为 Catalina  实例  执行的为 catalina 的 stop 方法
        Method method =
                catalinaDaemon.getClass().getMethod("getAwait", paramTypes);
        Boolean b=(Boolean)method.invoke(catalinaDaemon, paramValues);
        return b.booleanValue();
    }


    /**
     * Destroy the Catalina Daemon.
     */
    public void destroy() {

        // FIXME

    }

    public void stopServer(String[] arguments)
            throws Exception {

        Object param[];
        Class<?> paramTypes[];
        if (arguments==null || arguments.length==0) {
            paramTypes = null;
            param = null;
        } else {
            paramTypes = new Class[1];
            paramTypes[0] = arguments.getClass();
            param = new Object[1];
            param[0] = arguments;
        }

        //catalinaDaemon  对象为 Catalina  实例  执行的为 catalina 的 stop 方法
        Method method =
                catalinaDaemon.getClass().getMethod("stopServer", paramTypes);
        method.invoke(catalinaDaemon, param);

    }

    protected String replace(String str) {
        // Implementation is copied from ClassLoaderLogManager.replace(),
        // but added special processing for catalina.home and catalina.base.
        String result = str;
        int pos_start = str.indexOf("${");
        if (pos_start >= 0) {
            StringBuilder builder = new StringBuilder();
            int pos_end = -1;
            while (pos_start >= 0) {
                builder.append(str, pos_end + 1, pos_start);
                pos_end = str.indexOf('}', pos_start + 2);
                if (pos_end < 0) {
                    pos_end = pos_start - 1;
                    break;
                }
                String propName = str.substring(pos_start + 2, pos_end);
                String replacement;
                if (propName.length() == 0) {
                    replacement = null;
                } else if (Globals.CATALINA_HOME_PROP.equals(propName)) {
                    replacement = getCatalinaHome();
                } else if (Globals.CATALINA_BASE_PROP.equals(propName)) {
                    replacement = getCatalinaBase();
                } else {
                    replacement = System.getProperty(propName);
                }
                if (replacement != null) {
                    builder.append(replacement);
                } else {
                    builder.append(str, pos_start, pos_end + 1);
                }
                pos_start = str.indexOf("${", pos_end + 1);
            }
            builder.append(str, pos_end + 1, str.length());
            result = builder.toString();
        }
        return result;
    }

    private Object getServer() throws Exception {

        String methodName = "getServer";
        Method method =
                catalinaDaemon.getClass().getMethod(methodName);
        return method.invoke(catalinaDaemon);

    }

    private void setCatalinaHome() {

        if (System.getProperty(Globals.CATALINA_HOME_PROP) != null)
            return;
        File bootstrapJar =
                new File(System.getProperty("user.dir"), "bootstrap.jar");
        if (bootstrapJar.exists()) {
            try {
                System.setProperty
                        (Globals.CATALINA_HOME_PROP,
                                (new File(System.getProperty("user.dir"), ".."))
                                        .getCanonicalPath());
            } catch (Exception e) {
                // Ignore
                System.setProperty(Globals.CATALINA_HOME_PROP,
                        System.getProperty("user.dir"));
            }
        } else {
            System.setProperty(Globals.CATALINA_HOME_PROP,
                    System.getProperty("user.dir"));
        }

    }

    private void setCatalinaBase() {

        if (System.getProperty(Globals.CATALINA_BASE_PROP) != null)
            return;
        if (System.getProperty(Globals.CATALINA_HOME_PROP) != null)
            System.setProperty(Globals.CATALINA_BASE_PROP,
                    System.getProperty(Globals.CATALINA_HOME_PROP));
        else
            System.setProperty(Globals.CATALINA_BASE_PROP,
                    System.getProperty("user.dir"));

    }

    public static String getCatalinaHome() {
        return System.getProperty(Globals.CATALINA_HOME_PROP,
                System.getProperty("user.dir"));
    }

    public static String getCatalinaBase() {
        return System.getProperty(Globals.CATALINA_BASE_PROP, getCatalinaHome());
    }



}
