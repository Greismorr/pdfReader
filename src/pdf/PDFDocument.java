package pdf;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import interfaces.IDocument;

public class PDFDocument implements IDocument{
	private Class pdDocumentClass;
	private Object pdDocumentInstance;
	private Object pdfTextStripperInstance;
	private String pdfPage;
	private Method loadFile;
	private Method close;
	private Method getNumberOfPages;
	private Method getText;
	private Method setStartPage;
	private Method setEndPage;
    private int page;
    private int totalPages;
	
	public PDFDocument(URLClassLoader sysLoader) {
		try {
			this.pdDocumentClass = Class.forName("org.apache.pdfbox.pdmodel.PDDocument", true, sysLoader);
			this.pdfTextStripperInstance = Class.forName("org.apache.pdfbox.text.PDFTextStripper", true, sysLoader).newInstance();
			this.pdDocumentInstance = pdDocumentClass.newInstance();
			
			this.loadFile = pdDocumentClass.getDeclaredMethod("load", File.class);
			this.close = pdDocumentClass.getDeclaredMethod("close");
			this.getNumberOfPages = pdDocumentClass.getDeclaredMethod("getNumberOfPages");
			this.getText = pdfTextStripperInstance.getClass().getDeclaredMethod("getText", pdDocumentClass);
			this.setStartPage = pdfTextStripperInstance.getClass().getDeclaredMethod("setStartPage", int.class);
			this.setEndPage = pdfTextStripperInstance.getClass().getDeclaredMethod("setEndPage", int.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void open(File fileToOpen) {
		try {
			pdDocumentInstance = this.loadFile.invoke(pdDocumentClass, fileToOpen);
			this.totalPages = (Integer) this.getNumberOfPages.invoke(this.pdDocumentInstance);
			this.page = 1;
			
			if(documentIsReadable()) {
				this.getPage();
			}else {
				JOptionPane.showMessageDialog(null, "PDF document text is not readable.");
				this.totalPages = 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean documentIsReadable() {
        if(this.countAllLines().length <= 1) {
        	return false;
        }else {
        	return true;
        }
	}
	
	private void getPage() {
		try {
			this.textStripper(this.page);
			this.pdfPage = (String) this.getText.invoke(this.pdfTextStripperInstance, pdDocumentInstance);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String[] countAllLines() {
		String[] lines = null;
		
        for (int i = 1; i <= this.totalPages; i++) {
            try {
    			this.textStripper(i);
				lines = ((String) this.getText.invoke(this.pdfTextStripperInstance, this.pdDocumentInstance)).replaceAll("visiblespace", " ").split("\n");
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
		return lines;
	}
	
	private void textStripper(int page) {
		try {
			this.setStartPage.invoke(this.pdfTextStripperInstance, page);
			this.setEndPage.invoke(this.pdfTextStripperInstance, page);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JFrame getEditor() {
        JFrame mainFrame = null;
        
		try {
			mainFrame = new JFrame();
			JPanel mainPanel = new JPanel(new BorderLayout());
			JPanel textPanel = new JPanel();
			JPanel buttons = new JPanel();
			JMenuBar menuBar = new JMenuBar();
			JLabel nPagesLabel = new JLabel();
			JTextPane textArea = new JTextPane();
			JScrollPane scrollBar = new JScrollPane(textPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			StyledDocument doc = textArea.getStyledDocument();
			
			this.initFrame(mainFrame);
			this.initPageLabel(nPagesLabel);
			this.initTextArea(textArea);
			this.stylizeDoc(doc);
			this.createMenu(menuBar, textArea, nPagesLabel);
			this.createButtons(buttons, textArea, nPagesLabel, scrollBar);
			this.makeCaretNeverUpdate(textArea);
			
	        textPanel.add(textArea);
	        mainPanel.add(nPagesLabel, BorderLayout.PAGE_START);
	        mainPanel.add(scrollBar, BorderLayout.CENTER);
	        mainPanel.add(buttons, BorderLayout.PAGE_END);
	        mainFrame.setJMenuBar(menuBar);
	        mainFrame.add(mainPanel);
		} catch (Exception e1) {
			e1.printStackTrace();
		} 

		return mainFrame; 
	}
	
	private void initTextArea(JTextPane textArea) {
        textArea.setText(this.pdfPage);
        textArea.setEditable(false);
	}

	private void initPageLabel(JLabel nPagesLabel) {
		nPagesLabel.setText("Página " + this.page + " de " + this.totalPages);
	}

	private void initFrame(JFrame mainFrame) {
		mainFrame.setTitle("PDF Reader");
        mainFrame.setSize(800, 700);  
        mainFrame.setLocationRelativeTo(null);  
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
	}

	private void initButton(Component button) {
		button.setBounds(50, 50, 150, 20);
	}
	
	private void stylizeDoc(StyledDocument doc) {
		SimpleAttributeSet center = new SimpleAttributeSet();
		StyleConstants.setLineSpacing(center, 0.3F);
		StyleConstants.setFontSize(center, 12);
		StyleConstants.setAlignment(center, StyleConstants.ALIGN_JUSTIFIED);
		doc.setParagraphAttributes(1, doc.getLength(), center, false);
	}
	
	
	private void makeCaretNeverUpdate(JTextPane textArea) {
		DefaultCaret caret = (DefaultCaret)textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
	}
	
	private void createButtons(JPanel buttons, final JTextPane textArea, final JLabel nPagesLabel, final JScrollPane scrollBar) {
		JButton pageForwardButton = new JButton("Page Forward");  
		JButton pageBackwardButton = new JButton("Page Backward"); 
		
		pageForwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pageForward(textArea, nPagesLabel, scrollBar);
			}
		});
		
		pageBackwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pageBackward(textArea, nPagesLabel, scrollBar);
			}
		});
		
		this.initButton(pageBackwardButton);
		this.initButton(pageForwardButton);
		
        buttons.add(pageBackwardButton);
        buttons.add(pageForwardButton);
	}

	private final void pageForward(JTextPane textArea, JLabel nPagesLabel, JScrollPane scrollBar) {
		if(this.page < this.totalPages) {
			this.page += 1;
			this.updatePage(textArea, nPagesLabel);
		}
	}
	
	private final void pageBackward(JTextPane textArea, JLabel nPagesLabel, JScrollPane scrollBar) {
		if(this.page >= 2) {
			this.page -= 1;
			this.updatePage(textArea, nPagesLabel);
			scrollBar.getVerticalScrollBar().setValue(0);
		}
	}
	
	private void createMenu(JMenuBar menuBar, final JTextPane textArea, final JLabel nPagesLabel) {
		JMenu actionsMenu = new JMenu("Actions");
		
		JMenuItem openFileMenuItem = new JMenuItem(new AbstractAction("Open File"){
		    public void actionPerformed(ActionEvent e){	
				changeFile(textArea, nPagesLabel);
			}
		});
		
        actionsMenu.add(openFileMenuItem);
        menuBar.add(actionsMenu);
	}
	
	private final void changeFile(JTextPane textArea, JLabel nPagesLabel) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int fileSelected = fileChooser.showOpenDialog(fileChooser);
		  
		try {
			if(fileSelected == JFileChooser.APPROVE_OPTION){	
				this.close.invoke(this.pdDocumentInstance);
				
				File fileToOpen = fileChooser.getSelectedFile();
				this.open(fileToOpen);
				this.updatePage(textArea, nPagesLabel);
		  	}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updatePage(JTextPane textArea, JLabel nPagesLabel) {
		this.getPage();
		textArea.setText(this.pdfPage);
		this.initPageLabel(nPagesLabel);
	}
}
