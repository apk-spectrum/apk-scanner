package com.apkscanner.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import com.apkscanner.core.permissionmanager.PermissionManager;
import com.apkscanner.core.permissionmanager.PermissionRepository;
import com.apkscanner.core.permissionmanager.PermissionRepository.SourceCommit;
import com.apkscanner.gui.component.HtmlEditorPane;
import com.apkscanner.gui.component.KeyStrokeAction;
import com.apkscanner.gui.component.WindowSizeMemorizer;
import com.apkscanner.gui.component.tabbedpane.TabbedPaneUIManager;
import com.apkscanner.resource.RFile;
import com.apkscanner.resource.RImg;
import com.apkscanner.resource.RProp;
import com.apkscanner.resource.RStr;
import com.apkscanner.util.Log;

public class PermissionReferencePanel extends JPanel {
	private static final long serialVersionUID = 1224360539653858070L;

	private JDialog dialog;
	private HtmlEditorPane referencePanel;
	private HtmlEditorPane protectLevelPanel;

	public PermissionReferencePanel() {
		setLayout(new BorderLayout());
		JTabbedPane tabbedPanel = new JTabbedPane();
		String tabbedStyle = RProp.S.TABBED_UI_THEME.get();
		tabbedPanel.setOpaque(true);
		TabbedPaneUIManager.setUI(tabbedPanel, tabbedStyle);

		referencePanel = new HtmlEditorPane();
		referencePanel.setEditable(false);
		referencePanel.setOpaque(true);
		protectLevelPanel = new HtmlEditorPane();
		protectLevelPanel.setEditable(false);
		protectLevelPanel.setOpaque(true);
		tabbedPanel.addTab("Reference", new JScrollPane(referencePanel));
		tabbedPanel.addTab("Protection Level", new JScrollPane(protectLevelPanel));

		add(tabbedPanel, BorderLayout.CENTER);

		loadData();
	}

	private void loadData() {
		referencePanel.setText(RFile.RAW_PERMISSION_REFERENCE_HTML.getString());
		referencePanel.setCaretPosition(0);

		protectLevelPanel.setText(RFile.RAW_PROTECTION_LEVELS_HTML.getString());
		protectLevelPanel.setCaretPosition(0);

		PermissionRepository repo = PermissionManager.getPermissionRepository();
		if(repo == null) {
			Log.e("No have information of sources.");
			return;
		}

		if(repo.url == null) {
			Log.e("Repository url is null");
			return;
		}

		boolean isAOSP = repo.url.startsWith("https://android.googlesource.com/");
		String manifestPath = repo.manifestPath;
		if(manifestPath == null) {
			if(!isAOSP) {
				Log.e("manifestPath is null\n");
				return;
			} else {
				manifestPath = "/core/res/AndroidManifest.xml";
			}
		}
		String baseUrl = isAOSP ? repo.url + "/+/" : repo.url;
		String manifestUrl = baseUrl + "master" + manifestPath;

		StringBuilder sb = new StringBuilder();
		sb.append("<li>Repository : ").append(HtmlEditorPane.makeHyperLink(repo.url, isAOSP ? "AOSP" : repo.url, repo.url, null, null)).append("</li>");
		sb.append("<li>Manifest : ").append(HtmlEditorPane.makeHyperLink(manifestUrl, manifestPath, manifestUrl, null, null)).append("</li>");
		sb.append("<li>Resources : ");
		if(repo.config != null && repo.config.length > 0) {
			for(String config: repo.config) {
				String confPath = repo.resourcePath.replace("${config}", !config.equals("default") ? "-" + config : "");
				String resUrl = baseUrl + "master" + confPath;
				sb.append("<br/>").append(HtmlEditorPane.makeHyperLink(resUrl, confPath, resUrl, null, null));
			}
		} else {
			sb.append("None");
		}
		sb.append("</li>");
		referencePanel.setInnerHTMLById("repository-info", sb.toString());

		sb = new StringBuilder();
		for(SourceCommit sdk: repo.sources) {
			String oddeven = sdk.getSdkVersion() % 2 == 1 ? "odd" : "even";
			sb.append("<tr class=\"tr-").append(oddeven).append("\">");
			sb.append("<td>").append(sdk.getSdkVersion()).append("</td>");
			if(sdk.getCommitId() == null) {
				sb.append("<td>N/A</td>");
			} else {
				String url = baseUrl + sdk.getCommitId() + manifestPath;
				sb.append("<td>").append("<a href=\"").append(url).append("\" title=\"").append(url).append("\">").append(sdk.getCommitId()).append("</a>").append("</td>");
			}
			sb.append("\n");
		}
		referencePanel.insertElementLast("reference-table", sb.toString());
	}

	public void showDialog(Window owner) {
		dialog = new JDialog(owner);

		dialog.setTitle(RStr.LABEL_REFERENCE_N_LEVELS.get());
		dialog.setIconImage(RImg.APP_ICON.getImage());
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setResizable(true);

		dialog.setModal(false);
		dialog.setLayout(new BorderLayout());

		WindowSizeMemorizer.apply(dialog, new Dimension(450, 500));

		dialog.setLocationRelativeTo(owner);

		dialog.add(this, BorderLayout.CENTER);

		dialog.setVisible(true);

		KeyStrokeAction.registerKeyStrokeActions(dialog.getRootPane(), JComponent.WHEN_IN_FOCUSED_WINDOW, new KeyStroke[] {
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)
		}, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
	}
}
