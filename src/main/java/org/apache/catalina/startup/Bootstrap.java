package org.apache.catalina.startup;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
                log.warn("Bootstrap: command \"" + command + "\" does not exist.");
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

    private static void handleThrowable(Throwable t) {
        if (t instanceof ThreadDeath) {
            throw (ThreadDeath) t;
        }
        if (t instanceof VirtualMachineError) {
            throw (VirtualMachineError) t;
        }
        // All other instances of Throwable will be silently swallowed
    }


    public void init()
            throws Exception
    {

        // Set Catalina path
        setCatalinaHome();
        setCatalinaBase();

//        初始化 classLoader
        initClassLoaders();

        Thread.currentThread().setContextClassLoader(catalinaLoader);

        SecurityClassLoad.securityClassLoad(catalinaLoader);

        // Load our startup class and call its process() method
        if (log.isDebugEnabled())
            log.debug("Loading startup class");

        //加载 org.apache.catalina.startup.Catalina class
        Class<?> startupClass =
                catalinaLoader.loadClass
                        ("org.apache.catalina.startup.Catalina");

        // 实例化 Catalina 实例
        Object startupInstance = startupClass.newInstance();

        // Set the shared extensions class loader
        if (log.isDebugEnabled())
            log.debug("Setting startup class properties");

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
            commonLoader = createClassLoader("common", null);
            if( commonLoader == null ) {
                // no config file, default to this loader - we might be in a 'single' env.
                commonLoader=this.getClass().getClassLoader();
            }
            catalinaLoader = createClassLoader("server", commonLoader);
            sharedLoader = createClassLoader("shared", commonLoader);
        } catch (Throwable t) {
            handleThrowable(t);
            log.error("Class loader creation threw exception", t);
            System.exit(1);
        }
    }
}
