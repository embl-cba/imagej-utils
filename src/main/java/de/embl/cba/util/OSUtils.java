package de.embl.cba.util;

public class OSUtils
{

    public static String getOsName()
    {
        return System.getProperty("os.name");
    }

    public static boolean isWindows()
    {
        String OS = getOsName();

        if ( OS.toLowerCase().contains( "win" ) ) return true;

        return false;
    }

    public static boolean isMac()
    {
        String OS = getOsName();

        if ( OS.toLowerCase().contains( "mac" ) ) return true;
        if ( OS.toLowerCase().contains( "darwin" ) ) return true;

        return false;
    }

    public static boolean isUnix()
    {
        String OS = getOsName();

        if ( OS.toLowerCase().contains("nix")  ) return true;
        if ( OS.toLowerCase().contains("nux")  ) return true;
        if ( OS.toLowerCase().contains("aix")  ) return true;

        return false;
    }


}
