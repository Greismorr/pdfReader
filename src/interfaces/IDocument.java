package interfaces;

import java.io.File;
import javax.swing.JFrame;

public interface IDocument {
	public void open(File fileToOpen);
	public JFrame getEditor();
}
