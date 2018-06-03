package de.machmireinebook.epubeditor.epublib;

/**
 * Created by Michail Jungierek
 */
public enum EpubVersion
{
    UNKNOWN("-1"),
    VERSION_2("2.0"),
    VERSION_3("3.0"),
    VERSION_3_0_1("3.0.1"),
    VERSION_3_1("3.1"),
    VERSION_3_2("3.2"),
    ;

    private String version;

    EpubVersion(String version)
    {
        this.version = version;
    }

    public String getVersion()
    {
        return version;
    }

    public static EpubVersion getByString(String versionString)
    {
        for (EpubVersion currentVersion : EpubVersion.values())
        {
            if (currentVersion.version.equals(versionString))
            {
                return currentVersion;
            }
        }
        return UNKNOWN;
    }
}
