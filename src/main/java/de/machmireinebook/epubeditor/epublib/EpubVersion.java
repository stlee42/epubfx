package de.machmireinebook.epubeditor.epublib;

/**
 * Created by Michail Jungierek, Acando GmbH on 06.10.2017
 */
public enum EpubVersion
{
    UNKNOWN(-1),
    VERSION_2(2.0),
    VERSION_3(3.0),
    VERSION_3_1(3.1);

    private double version;

    EpubVersion(double version)
    {
        this.version = version;
    }

    public double getVersion()
    {
        return version;
    }

    public static EpubVersion getByString(String versionString)
    {
        double version = Double.parseDouble(versionString);
        for (EpubVersion currentVersion : EpubVersion.values())
        {
            if (currentVersion.version == version)
            {
                return currentVersion;
            }
        }
        return UNKNOWN;
    }
}
