import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import interfaces.IDocument;
import pdf.PDFDocument;
import java.lang.ClassLoader;

public class Main {
	public static URL[] loadJarsUrl(ArrayList<String> fileTypes){
		File pluginDir = new File("./plugins");
	  	String[] plugins = pluginDir.list();
	  	URL[] jars = new URL[plugins.length];
	  	
	  	for(int i = 0; i < plugins.length; i++){
	  		try{		  		
				jars[i] = (new File("./plugins/" + plugins[i])).toURI().toURL();
				String pathToString = jars[i].toString();
				String pluginName = pathToString.substring(pathToString.lastIndexOf('/') + 1).replace(".jar", "");

				fileTypes.add(pluginName);
			}catch(MalformedURLException error){
				error.printStackTrace();
			}
	  	}
	  	
	  	return jars;
	}
	
	private static final Class[] parameters = new Class[]{URL.class};
	
	public static URLClassLoader getSysLoader(URL[] jars) {
		URLClassLoader sysLoader = null;
		
		try {
			
			sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			Class sysClass = URLClassLoader.class;
			Method method = sysClass.getDeclaredMethod("addURL", parameters);
			
			method.setAccessible(true);
			
			for(URL jar : jars) {
				method.invoke(sysLoader, new Object[]{jar});
				String jarString = jar.toString();
				String jarName = jarString.substring(jarString.lastIndexOf("/") + 1);
				System.out.println(jarName + " loaded successfully!");
			}		
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sysLoader;
	}
	
	public static void main(String []args) throws Exception{
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int fileSelected = fileChooser.showOpenDialog(fileChooser);
		  
		if(fileSelected == JFileChooser.APPROVE_OPTION){
			File fileToOpen = fileChooser.getSelectedFile();
		  	String fileName = fileToOpen.getName();
		  	String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
		  	ArrayList<String> fileTypes = new ArrayList<String>();
			URL[] jars = loadJarsUrl(fileTypes);
			
			if(fileTypes.contains(fileType)) {
				URLClassLoader sysLoader = getSysLoader(jars);
				
				IDocument pdfDoc = new PDFDocument(sysLoader);
				pdfDoc.open(fileToOpen);
				JFrame mainFrame = pdfDoc.getEditor();
				mainFrame.setVisible(true);
			}else {
				JOptionPane.showMessageDialog(null, "Unsupported File.");
			}
	  	}
	}
}
