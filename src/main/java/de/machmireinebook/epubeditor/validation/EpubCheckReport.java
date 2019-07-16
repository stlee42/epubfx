package de.machmireinebook.epubeditor.validation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.adobe.epubcheck.api.EPUBLocation;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.messages.LocalizedMessageDictionary;
import com.adobe.epubcheck.messages.Message;
import com.adobe.epubcheck.messages.MessageDictionary;
import com.adobe.epubcheck.messages.MessageId;
import com.adobe.epubcheck.messages.Severity;
import com.adobe.epubcheck.util.FeatureEnum;
import com.adobe.epubcheck.util.ReportingLevel;

/**
 * User: Michail Jungierek
 * Date: 16.07.2019
 * Time: 20:57
 */
public class EpubCheckReport implements Report
{
    private List<ValidationMessage> messages = new ArrayList<>();
    private String epubFileName;
    private int errorCount;
    private int warningCount;
    private int fatalErrorCount;
    private int usageCount;
    private int infoCount;
    private int reportingLevel;

    private MessageDictionary dictionary = new LocalizedMessageDictionary();


    @Override
    public int getErrorCount()
    {
        return errorCount;
    }

    @Override
    public int getWarningCount()
    {
        return warningCount;
    }

    @Override
    public int getFatalErrorCount()
    {
        return fatalErrorCount;
    }

    @Override
    public int getInfoCount()
    {
        return infoCount;
    }

    @Override
    public int getUsageCount()
    {
        return usageCount;
    }

    @Override
    public int generate()
    {
        return 0;
    }

    @Override
    public void initialize()
    {

    }

    @Override
    public void setEpubFileName(String epubFileName)
    {
        this.epubFileName = epubFileName;
    }

    @Override
    public String getEpubFileName()
    {
        return epubFileName;
    }

    @Override
    public void setCustomMessageFile(String s)
    {

    }

    @Override
    public String getCustomMessageFile()
    {
        return null;
    }

    @Override
    public int getReportingLevel()
    {
        return reportingLevel;
    }

    @Override
    public void setReportingLevel(int reportingLevel)
    {
        this.reportingLevel = reportingLevel;
    }

    @Override
    public void close()
    {

    }

    @Override
    public void setOverrideFile(File file)
    {

    }

    @Override
    public MessageDictionary getDictionary()
    {
        return dictionary;
    }

    @Override
    public void message(MessageId id, EPUBLocation epubLocation, Object... args)
    {
        Message message = getDictionary().getMessage(id);
        assert (message != null);
        Severity severity = message.getSeverity();
        if (ReportingLevel.getReportingLevel(severity) >= getReportingLevel())
        {
            if (severity.equals(Severity.ERROR))
            {
                errorCount++;
            }
            else if (severity.equals(Severity.WARNING))
            {
                warningCount++;
            }
            else if (severity.equals(Severity.FATAL))
            {
                fatalErrorCount++;
            }
            else if (severity.equals(Severity.USAGE))
            {
                usageCount++;
            }
            else if (severity.equals(Severity.INFO))
            {
                infoCount++;
            }
            this.message(message, epubLocation, args);
        }
    }

    @Override
    public void message(Message message, EPUBLocation epubLocation, Object... args)
    {
        messages.add(new ValidationMessage(message.getSeverity().toString(), epubLocation.getPath(), epubLocation.getLine(), epubLocation.getLine(), message.getMessage(args)));
    }

    @Override
    public void info(String s, FeatureEnum featureEnum, String s2)
    {
//            messages.add(new ValidationMessage("Information", s, -1, -1, s2));
    }

    public List<ValidationMessage> getMessages()
    {
        return messages;
    }
}
