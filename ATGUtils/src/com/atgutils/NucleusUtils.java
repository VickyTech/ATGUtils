package com.atgutils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.xml.sax.SAXException;

import atg.adapter.gsa.xml.TemplateParser;
import atg.applauncher.AppLauncher;
import atg.applauncher.AppLauncherException;
import atg.applauncher.AppModule;
import atg.applauncher.LocalAppModuleManager;
import atg.applauncher.dynamo.DynamoServerLauncher;
import atg.nucleus.PropertyConfigurationFinder;
import atg.xml.Combiner;
import atg.xml.XMLFileException;

public class NucleusUtils {

    public static void main(String[] args) {

        usage();

    }

    private static void orderConfigPaths() {
        System.out.println("Enter the list of modules separated by ;(eg;DSS;DCS)");
        Scanner scanner = new Scanner(System.in);
        String moduleList = scanner.next();
        // System.out.println("system root:" + System.getProperty("atg.dynamo.root"));
        LocalAppModuleManager appManager = new LocalAppModuleManager(new File(System.getProperty("atg.dynamo.root")));

        // List<String> modules = new ArrayList<String>();
        // modules.add("DAS");
        // modules.add("ROGERSStore.asset-management");
        // modules.add("DCS");
        // modules.add("DSS");

        try {

            AppLauncher appLaunher = AppLauncher.getLauncher(appManager, moduleList);
            // AppLauncher appLaunher = new AppLauncher(appManager, modules);
            // List<AppModule> modulesdep = appLaunher.getDependentModules(modules);
            List<AppModule> modulesdep = appLaunher.getModules();
            System.out.println("\n");
            System.out.println("modules size:" + modulesdep.size());

            for (AppModule moduleName : modulesdep) {
                System.out.println("moduleName name:" + moduleName.getName());
            }
            System.out.println("\n");
            String configPaths = DynamoServerLauncher.calculateConfigPath(appLaunher, true, null, true, null);
            StringTokenizer st = new StringTokenizer(configPaths, File.pathSeparator);
            System.out.println("Following are the config Path in the order in which Nuclues searches - (least dependant to most dependant)");
            int i = 1;
            while (st.hasMoreTokens()) {

                String token = st.nextToken();
                System.out.println(i + "." + token);
                i++;
            }
        } catch (AppLauncherException e) {

            e.printStackTrace();
        }
    }

    private static void expandCmdArgs(String option) {

        if (option != null) {
            if (option.equals("1")) {
                orderConfigPaths();
            } else if (option.equals("2")) {
                propertyCombination();
            } else if (option.equals("3")) {
                xmlCombination();
            } else if (option.equals("4")) {
                sqlGeneration();
            }
            doContinue();
        } else {
            usage();
        }

    }

    private static void sqlGeneration() {
        System.out.println("Specify a single Module name containing your repository defintion file");
        Scanner scanner = new Scanner(System.in);
        String moduleList = scanner.nextLine();
        System.out.println("SQL file output path");
        String filePath = scanner.nextLine();
        System.out.println("Specify the Repository component path");
        String repoPath = scanner.nextLine();
        System.out.println("Repository component path:" + repoPath);
        DynamoServerLauncher launcher = new DynamoServerLauncher();
        String strModuleConfig = null;
        String classpathString = null;
        try {
            Map<String, String> env = launcher.generateEnvironment(moduleList, null, null, null, false, null);
            strModuleConfig = (String) env.get("CONFIGPATH");
            classpathString = (String) env.get("CLASSPATH");

        } catch (AppLauncherException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("FilePath:" + filePath);
        String configPath =
                strModuleConfig + ";" + System.getProperty("atg.dynamo.root") + "/DAS/config/dtmconfig.jar;"
                        + System.getProperty("atg.dynamo.root") + "/home/localconfig;" + System.getProperty("atg.dynamo.root")
                        + "/DAF/config/dafconfig.jar";

        System.out.println("configPath set:" + configPath);

        System.out.println("before classpath:" + System.getProperty("java.class.path"));
        System.out.println("classpath set:" + classpathString);
        System.setProperty("java.class.path", classpathString);
        String[] configurationPaths =
                {"-cmd", "startSQLRepository", "-configPath", configPath, "-repository", repoPath, "-outputSQLFile", filePath };

        TemplateParser.main(configurationPaths);
    }

    private static void doContinue() {
        System.out.println("Do you want to continue?Type y  to continue or n to exit");
        Scanner scanner = new Scanner(System.in);
        String res = scanner.next();
        if ("y".equalsIgnoreCase(res)) {
            usage();
        } else {
            System.exit(1);
        }

    }

    private static void xmlCombination() {

        System.out.println("Specify the Module names separated by ;");
        Scanner scanner = new Scanner(System.in);
        String moduleList = scanner.nextLine();
        System.out.println("file output path");
        String filePath = scanner.nextLine();
        System.out.println("Specify the XML Definition file path");
        String xmlPath = scanner.nextLine();
        System.out.println("XMLPath:" + xmlPath);

        System.out.println("FilePath:" + filePath);
        String[] configurationPaths = {"-m", moduleList, "-o", filePath, xmlPath };

        try {
            Combiner.main(configurationPaths);
        } catch (XMLFileException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        } catch (SAXException e) {

            e.printStackTrace();
        } catch (AppLauncherException e) {

            e.printStackTrace();
        }

    }

    private static void propertyCombination() {
        System.out.println("Specify the Module names separated by ;");
        Scanner scanner = new Scanner(System.in);
        String moduleList = scanner.nextLine();
        System.out.println("Specify the componentPath");
        String componentPath = scanner.nextLine();
        System.out.println("componentPath:" + componentPath);
        String[] configurationPaths = {"-m", moduleList, componentPath };
        PropertyConfigurationFinder.main(configurationPaths);

    }

    private static void usage() {

        System.out.println("\n");
        System.out.println(" Following are the options");
        System.out.println(" 1. orderConfigPaths");
        System.out.println(" 2. propertyCombination");
        System.out.println(" 3. xmlCombination");
        System.out.println(" 4. DDL scripts for repository definition");
        System.out.println("\n");

        Scanner scanner = new Scanner(System.in);

        String option = scanner.next();
        expandCmdArgs(option);

    }
}
