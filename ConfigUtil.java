package com.huawei.osm.knowledge.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.huawei.osm.common.log.OSMCommonLog;

public class ConfigUtil
{
    
    //    public static final String DEFAULT_CONF_CONFIG = System
    //            .getProperty("user.dir") + File.separator + "conf";
    public static final String DEFAULT_CONF_CONFIG = ".." + File.separator
            + "conf";
    
    private static Properties properties = null;
    
    //    private static final String CONF_PATH = "CONF_PATH";
    
    private static String path;
    
    public static String getPath()
    {
        return path;
    }
    
    public static void setPath(String path)
    {
        ConfigUtil.path = path;
    }
    
    public static String getConfPath()
    {
        
        path = System.getProperty("CONF_PATH");
        if (StringUtils.isBlank(path))
        {
            path = DEFAULT_CONF_CONFIG;
        }
        OSMCommonLog.info("The conf path is: path = {}", path);
        return path;
    }
    
    private static void init()
    {
        InputStreamReader reader = null;
        InputStream is = null;
        try
        {
            String str = ConfigUtil.getConfPath();
            File file = FileUtils.getFile(str);
            //            OSMCommonLog.info("Conf path = {}", file.getName());
            //            OSMCommonLog.info("Conf AbsolutePath = {}", file.getAbsolutePath());
            if (file != null)
            {
                properties = new Properties();
                List<File> fileList = getPropertiesFileList(file);
                if (fileList != null)
                {
                    for (File f : fileList)
                    {
                        OSMCommonLog.info("File name is: filename = {}",
                                f.getAbsolutePath());
                        String realPath = f.getAbsolutePath();
                        is = new FileInputStream(FileUtils.getFile(realPath));
                        reader = new InputStreamReader(is, "utf-8");
                        properties.load(reader);
                        is.close();
                        reader.close();
                    }
                }
            }
        }
        catch (IOException e)
        {
            OSMCommonLog.error("Load configuration files Fail.", e);
        }
        
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                    is = null;
                }
                catch (IOException e)
                {
                    is = null;
                }
            }
            if (reader != null)
            {
                try
                {
                    reader.close();
                    reader = null;
                }
                catch (IOException e)
                {
                    reader = null;
                }
            }
        }
    }
    
    /**
     * 获取conf目录下所有properties中key对应的value
     * @param key
     * @return 
     *
     * @see [类、类#方法、类#成员]
     */
    public static String getProperty(String key)
    {
        if (properties == null)
        {
            init();
        }
        if (properties != null)
        {
            return properties.getProperty(key);
        }
        return null;
    }
    
    /**
     * 获取所有的.properties文件（包括子目录）
     * @param file
     * @return 
     *
     * @see [类、类#方法、类#成员]
     */
    public static List<File> getPropertiesFileList(File file)
    {
        if (file == null)
        {
            return null;
        }
        if (!file.exists())
        {
            return null;
        }
        
        List<File> fileList = new ArrayList<File>();
        if (file.isFile())
        {
            if (file.getName().endsWith(".properties"))
            {
                fileList.add(file);
                return fileList;
            }
            else
            {
                return fileList;
            }
        }
        
        File[] files = file.listFiles();
        if (files != null)
        {
            for (int i = 0; i < files.length; i++)
            {
                List<File> fileListTemp = getPropertiesFileList(files[i]);
                if (fileListTemp != null)
                {
                    fileList.addAll(fileListTemp);
                }
            }
        }
        
        return fileList;
    }
}
