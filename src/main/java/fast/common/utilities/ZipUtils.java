package fast.common.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import fast.common.logging.FastLogger;  
  
  
public class ZipUtils {  
  
    /** 
     * The result file, output zip file 
     */  
	private File targetFile;
	private static FastLogger logger = FastLogger.getLogger("ZipUtils"); 

    public File getTargetFile() {
		return targetFile;
	}

	public void setTargetFile(File targetFile) {
		this.targetFile = targetFile;
	}

	public ZipUtils() {}  
      
    public ZipUtils(File target) {  
        targetFile = target;  
        if (targetFile.exists())  
            targetFile.delete();  
    }  
  
    /** 
     * Zip file
     *  
     * @param srcfile 
     */  
    public void zipFiles(File srcfile) {  
  
        ZipOutputStream out = null;  
        try (FileOutputStream fileOutputStream = new FileOutputStream(targetFile);){  
            
			out = new ZipOutputStream(fileOutputStream);  
              
            if(srcfile.isFile()){  
                zipFile(srcfile, out, "");  
            } else{  
                File[] list = srcfile.listFiles();  
                for (int i = 0; i < list.length; i++) {  
                    compress(list[i], out, "");  
                }  
            }    
        } catch (Exception e) {  
            logger.error(e.getMessage());
        } finally {  
            try {  
                if (out != null)  
                    out.close();  
            } catch (IOException e) {  
            	logger.error(e.getMessage());
            }  
        }  
    }  
  
    /** 
     * Compress the child file 
     * @param file 
     * @param out 
     * @param basedir 
     */  
    private void compress(File file, ZipOutputStream out, String basedir) {  
        if (file.isDirectory()) {  
            this.zipDirectory(file, out, basedir);  
        } else {  
            this.zipFile(file, out, basedir);  
        }  
    }  
  
    /** 
     * Compress single file
     *  
     * @param srcfile 
     */  
    public void zipFile(File srcfile, ZipOutputStream out, String basedir) {  
		if (!srcfile.exists())
			return;

		byte[] buf = new byte[1024];

		try (FileInputStream in = new FileInputStream(srcfile);){
			
			int len = in.read(buf);
			out.putNextEntry(new ZipEntry(basedir + srcfile.getName()));
			while (len > 0) {
				out.write(buf, 0, len);
				len = in.read(buf);
			}
		} catch (Exception e) {
			logger.error("1 " +e.getMessage());
		} finally {
			try {
				if (out != null)
					out.closeEntry();
			} catch (IOException e) {
				logger.error("2 " + e.getMessage());
			}
		}
    }  
  
    /** 
     * Compress Directory
     * @param dir 
     * @param out 
     * @param basedir 
     */  
    public void zipDirectory(File dir, ZipOutputStream out, String basedir) {  
        if (!dir.exists())  
            return;  
  
        File[] files = dir.listFiles();  
        for (int i = 0; i < files.length; i++) {  
            compress(files[i], out, basedir + dir.getName() + "/");  
        }  
    }  
  
    public void clear(){
		if (targetFile != null)
			targetFile.delete();
    }  
  
}  
