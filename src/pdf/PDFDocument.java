package pdf;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import interfaces.IDocument;

public class PDFDocument implements IDocument{
	private Class pdDocumentClass;
	private Object pdPageInstance;
	private Object pdDocumentInstance;
	private Object pdfTextStripperInstance;
	private Method loadFile;
	private Method getPage;
	private Method close;
	private Method getNumberOfPages;
	private Method getContents;
	private Method getText;
	
	public PDFDocument(URLClassLoader sysLoader) {
		try {
			this.pdDocumentClass = Class.forName("org.apache.pdfbox.pdmodel.PDDocument", true, sysLoader);
			this.pdfTextStripperInstance = Class.forName("org.apache.pdfbox.text.PDFTextStripper", true, sysLoader).newInstance();
			this.pdDocumentInstance = pdDocumentClass.newInstance();
			
			this.loadFile = pdDocumentClass.getDeclaredMethod("load", File.class);
			this.close = pdDocumentClass.getDeclaredMethod("close");
			this.getPage = pdDocumentClass.getDeclaredMethod("getPage", int.class);
			this.getNumberOfPages = pdDocumentClass.getDeclaredMethod("getNumberOfPages");
			this.getContents = pdPageInstance.getClass().getDeclaredMethod("getContents");
			this.getText = pdfTextStripperInstance.getClass().getDeclaredMethod("getText", pdDocumentClass);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void open(File fileToOpen) {
		try {
			pdDocumentInstance = this.loadFile.invoke(pdDocumentClass, fileToOpen);
			
			this.pdfPage = this.getPage.invoke(pdDocumentInstance);
			this.close.invoke(pdDocumentInstance);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public final void changePdf(JTextPane textArea) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int fileSelected = fileChooser.showOpenDialog(fileChooser);
		  
		try {
			System.out.println(this.getNumberOfPages.invoke(this.pdDocumentInstance));
			
			if(fileSelected == JFileChooser.APPROVE_OPTION){
				File fileToOpen = fileChooser.getSelectedFile();
				this.open(fileToOpen);
				textArea.setText((String) this.pdfPage);
		  	}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	public JFrame getEditor() {
        JFrame mainFrame = null;
        
		try {
			mainFrame = new JFrame("PDF Reader");
			JPanel panel = new JPanel();
			JMenuBar menuBar = new JMenuBar();
			JMenu actionsMenu = new JMenu("Actions");
			JLabel nPages = new JLabel(this.getNumberOfPages.invoke(this.pdDocumentInstance).toString());
			
			final JTextPane textArea = new JTextPane();
			JScrollPane scrollBar=new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			
			JMenuItem openFileButton = new JMenuItem(new AbstractAction("Open File"){
			    public void actionPerformed(ActionEvent e){	
					changePdf(textArea);
				}
			});
			
			StyledDocument doc = textArea.getStyledDocument();
			SimpleAttributeSet center = new SimpleAttributeSet();
			StyleConstants.setLineSpacing(center, 1);
			StyleConstants.setFontSize(center, 12);
			StyleConstants.setAlignment(center, StyleConstants.ALIGN_JUSTIFIED);
			doc.setParagraphAttributes(0, doc.getLength(), center, false);
	        
			panel.setLayout(new BorderLayout());
	        textArea.setText((String) this.pdfPage);
	        textArea.setEditable(false);
	        panel.add(textArea);
	        actionsMenu.add(openFileButton);
	        menuBar.add(actionsMenu);
	        mainFrame.setJMenuBar(menuBar);
	        mainFrame.add(scrollBar);
	        mainFrame.add(nPages);
	        mainFrame.setSize(900, 800);  
	        mainFrame.setLocationRelativeTo(null);  
	        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
		} catch (Exception e1) {
			e1.printStackTrace();
		} 

		return mainFrame; 
	}

}
