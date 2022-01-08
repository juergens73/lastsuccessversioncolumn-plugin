/*
 * The MIT License
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Alan Harder
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.plugins.lastsuccessversioncolumn;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * View column that shows the last success version by parsing it out of the build description or using the build number.
 * It also shows the date it succeeded.
 * 
 * @author Adam Purkiss
 */
public class LastSuccessVersionColumn extends ListViewColumn {
    
    private int buildStatus = 0;
    
    @DataBoundConstructor
    public LastSuccessVersionColumn(int buildStatus) {
        super();
        this.buildStatus = buildStatus;
    }
    
    public LastSuccessVersionColumn() {
        this(0);
    }
    
    public int getBuildStatus() {
        return buildStatus;
    }
    
    public String getDataName(Job job) {
        Map<String,Object> data = getJobData(job);
        return MessageFormat.format("{0}-{1,date,yyyyMMdd-HHmmss}", data.get("buildNumber"), data.get("timestamp"));
    }
    
    public String getTimestamp(Job<?,?> job) {
        return DateFormat.getDateTimeInstance().format((Date)getJobData(job).get("timestamp"));
    }
    
    public String getShortName(Job<?,?> job) {
        StringBuffer content = new StringBuffer();
        String tempDescription = (String) getJobData(job).get("description");
        int index = -1;
        
        if (tempDescription != null) {
            index = tempDescription.indexOf("[version]");
        }
        
        if (index != -1) {
            content.append(tempDescription.substring(index + 9).trim());
        } else {
            content.append(String.valueOf(getJobData(job).get("buildNumber")));
        }
        
        return content.toString();
    }
    
    public String getJobURL(Job<?,?> job) {
        return (String) getJobData(job).get("url");
    }
    
    private Map<String,Object> getJobData(Job<?,?> job) {
        Run<?,?> lastSuccessfulBuild = getLastBuild(job);
        Map<String,Object> jobData = new HashMap<>();
        jobData.put("buildNumber", 0);
        jobData.put("timestamp", new Date());
        jobData.put("url", "#");
        jobData.put("description", "N/A");
        
        if (lastSuccessfulBuild != null) {
            jobData.put("buildNumber", lastSuccessfulBuild.getNumber());
            jobData.put("timestamp", lastSuccessfulBuild.getTimestamp().getTime());
            jobData.put("url", "/"+lastSuccessfulBuild.getUrl());
            jobData.put("description", lastSuccessfulBuild.getDescription());
        }
        return jobData;        
    }
    
    private Run<?,?> getLastBuild(Job<?,?> job) {
        return (buildStatus == 1 ? job.getLastFailedBuild() : job.getLastSuccessfulBuild());
    }
    
    @Extension
    public static class DescriptorImpl extends ListViewColumnDescriptor {
        
        @Override
        public boolean shownByDefault() {
            return false;
        }

        @Override
        public String getDisplayName() {
            return Messages.LastSuccessVersionColumn_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "#";
        }
    }    
}
