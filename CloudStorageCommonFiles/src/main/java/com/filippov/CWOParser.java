package com.filippov;

public class CWOParser {

    public static void parselResolver(CloudWrappedObject cwo) {
        switch (cwo.getTypeEnum()) {
            case COMMAND: resolveCommand(cwo); break;
            case FILE: resolveFile(cwo); break;
            case SEPARATEDFILE: resolveSeparatedFile(cwo); break;
            default: resolveError(); break;
        }
    }

    private static void resolveError() {
        System.out.println("Неизвестная команда или данные повреждены");
    }

    private static void resolveSeparatedFile(CloudWrappedObject cwo) {

    }

    private static void resolveFile(CloudWrappedObject cwo) {

    }

    private static void resolveCommand(CloudWrappedObject cwo) {
    }

}
