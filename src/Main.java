import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.lang.ClassLoader;

public class Main {
	public static URL[] loadJarsUrl(Hashtable<String, String> fileTypes){
		File pluginDir = new File("./plugins");
	  	String[] plugins = pluginDir.list();
	  	URL[] jars = new URL[plugins.length];
	  	
	  	for(int i = 0; i < plugins.length; i++){
	  		try{		  		
				jars[i] = (new File("./plugins/" + plugins[i])).toURI().toURL();
				String pathToString = jars[i].toString();
				String pluginName = pathToString.substring(pathToString.lastIndexOf('/') + 1).replace(".jar", "");
				String pluginType = pluginName.replace("Document", "").toLowerCase();
				
				fileTypes.put(pluginType, pluginName);
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
		String env = "user.home";
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty(env)));
		int fileSelected = fileChooser.showOpenDialog(fileChooser);
		  
		if(fileSelected == JFileChooser.APPROVE_OPTION){
			File fileToOpen = fileChooser.getSelectedFile();
		  	String fileName = fileToOpen.getName();
		  	String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
		  	Hashtable<String, String> fileTypes = new Hashtable<String, String>();
			URL[] jars = loadJarsUrl(fileTypes);
			
			if(fileTypes.containsKey(fileType)) {
				String pluginName = fileTypes.get(fileType);
				String pluginClassName = fileType + '.' + pluginName;
				String targetClassName = "interfaces.IDocument";
				
				URLClassLoader sysLoader = getSysLoader(jars);
				Class adapterClass = Class.forName(pluginClassName, true, sysLoader);
				Class targetClass = Class.forName(targetClassName, true, sysLoader);
				Method open = targetClass.getDeclaredMethod("open", File.class);
				Method getEditor = targetClass.getDeclaredMethod("getEditor");
				
				Constructor adapterConstructor = adapterClass.getConstructor(URLClassLoader.class);
				Object adapteeDoc = targetClass.cast(adapterConstructor.newInstance(sysLoader));
				
				open.invoke(adapteeDoc, fileToOpen);
				JFrame mainFrame = (JFrame) getEditor.invoke(adapteeDoc);
				mainFrame.setVisible(true);
			}else {
				JOptionPane.showMessageDialog(null, "Unsupported File.");
			}
	  	}
	}
}
