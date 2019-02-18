package com.apkscanner.gui.util;

import java.awt.Desktop;
import java.awt.Font;
import java.io.IOException;
import java.net.URI;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import com.apkscanner.util.Log;

public class JHtmlEditorPane extends JEditorPane implements HyperlinkListener
{
	private static final long serialVersionUID = 7856109068620039501L;

	private String head = null;
	private String body = null;

	private String tooltip;
	private StyleSheet styleSheet;

	public abstract interface HyperlinkClickListener {
		public abstract void hyperlinkClick(String id);
	}

	private HyperlinkClickListener hyperlinkClickListener;

	public JHtmlEditorPane()
	{
		this("", "", "");
	}

	public JHtmlEditorPane(String head, String style, String body)
	{
		super("text/html", null);
		addHyperlinkListener(this);

		HTMLEditorKit kit = new ImgBaselineHTMLEditorKit();
		setEditorKit(kit);

		styleSheet = kit.getStyleSheet();
		styleSheet.addRule(style);
		styleSheet.addRule(makeFontStyleRule());
		setOpaque(false);
		setHtml(head, body);
	}

	private String makeFontStyleRule()
	{
		Font font = getFont();
		StringBuilder style = new StringBuilder("body { font-family: ");
		style.append(font.getFamily());
		style.append("; font-weight: ");
		style.append(font.isBold() ? "bold" : "normal");
		style.append("; font-size: ");
		style.append(font.getSize());
		style.append("pt; }");
		return style.toString();
	}

	@Override
	public void setText(String text) {
		if(head != null) {
			setHtml(head, text);
		} else {
			super.setText(text);
		}
	}

	public void setHtml(String head, String body)
	{
		if(head != null && head.trim().isEmpty()) {
			head = null;
		}
		if(objEquals(this.head, head) && objEquals(this.body, body)) {
			Log.v("same content to pre");
			return;
		}
		this.head = head;
		this.body = body;

		super.setText(makeHtml(head, body));
	}

	public void setHead(String head)
	{
		setHtml(head, body);
	}

	public void addStyleRule(String style)
	{
		styleSheet.addRule(style);
	}

	public void setBody(String body)
	{
		setHtml(head, body);
	}

	private static String makeHtml(String head, String body)
	{
		return head != null ? "<html><head>" + head + "</head><body>" + body + "</body></html>" : body;
	}

	public Element getElementById(String id) {
		HTMLDocument doc = (HTMLDocument)getDocument();
		return doc.getElement(id);
	}

	public Object getElementModelById(String id) {
		Object model = null;
		Element element = getElementById(id);
		if(element != null) {
			model = element.getAttributes().getAttribute(StyleConstants.ModelAttribute);
		}
		return model;
	}

	public void setInnerHTML(Element elem, String htmlText) {
		HTMLDocument doc = (HTMLDocument)getDocument();
		try {
			doc.setInnerHTML(elem, htmlText);
		} catch (BadLocationException | IOException e) {
			e.printStackTrace();
		}
	}

	public void setOuterHTML(Element elem, String htmlText) {
		HTMLDocument doc = (HTMLDocument)getDocument();
		try {
			doc.setOuterHTML(elem, htmlText);
		} catch (BadLocationException | IOException e) {
			e.printStackTrace();
		}
	}

	public String getInnerText(Element elem) {
		if(elem != null) {
			HTMLDocument doc = (HTMLDocument)getDocument();
			try {
				return doc.getText(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public void insertElementAfter(Element elem, String htmlText) {
		HTMLDocument doc = (HTMLDocument)getDocument();
		try {
			doc.insertAfterEnd(elem, htmlText);
		} catch (BadLocationException | IOException e) {
			e.printStackTrace();
		}
	}

	public void insertElementBefore(Element elem, String htmlText) {
		HTMLDocument doc = (HTMLDocument)getDocument();
		try {
			doc.insertBeforeStart(elem, htmlText);
		} catch (BadLocationException | IOException e) {
			e.printStackTrace();
		}
	}

	public void insertElementFirst(Element elem, String htmlText) {
		HTMLDocument doc = (HTMLDocument)getDocument();
		try {
			doc.insertAfterStart(elem, htmlText);
		} catch (BadLocationException | IOException e) {
			e.printStackTrace();
		}
	}

	public void insertElementLast(Element elem, String htmlText) {
		HTMLDocument doc = (HTMLDocument)getDocument();
		try {
			doc.insertBeforeEnd(elem, htmlText);
		} catch (BadLocationException | IOException e) {
			e.printStackTrace();
		}
	}

	public void removeElementById(Element elem) {
		HTMLDocument doc = (HTMLDocument)getDocument();
		doc.removeElement(elem);
	}

	public void setInnerHTMLById(String id, String htmlText) {
		setInnerHTML(getElementById(id), htmlText);
	}

	public void setOuterHTMLById(String id, String htmlText) {
		setOuterHTML(getElementById(id), htmlText);
	}

	public String getInnerText(String id) {
		return getInnerText(getElementById(id));
	}

	public void insertElementAfter(String id, String htmlText) {
		insertElementAfter(getElementById(id), htmlText);
	}

	public void insertElementBefore(String id, String htmlText) {
		insertElementBefore(getElementById(id), htmlText);
	}

	public void insertElementFirst(String id, String htmlText) {
		insertElementFirst(getElementById(id), htmlText);
	}

	public void insertElementLast(String id, String htmlText) {
		insertElementLast(getElementById(id), htmlText);
	}

	public void removeElementById(String id) {
		removeElementById(getElementById(id));
	}

	public void setHyperlinkClickListener(HyperlinkClickListener listener)
	{
		hyperlinkClickListener = listener;
	}

	@Override
	public void hyperlinkUpdate(HyperlinkEvent e)
	{
		JEditorPane editor = (JEditorPane) e.getSource();
		if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
			if(e.getDescription().isEmpty()) return;
			if(!e.getDescription().startsWith("@")) {
				Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
				if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			        try {
			            desktop.browse(new URI(e.getURL().toString()));
			        } catch (Exception e1) {
			            e1.printStackTrace();
			        }
			    }
			} else {
				Element elem = e.getSourceElement();
				if (elem != null) {
					AttributeSet attr = elem.getAttributes();
					AttributeSet a = (AttributeSet) attr.getAttribute(HTML.Tag.A);
					if (a != null && hyperlinkClickListener != null) {
						hyperlinkClickListener.hyperlinkClick((String) a.getAttribute(HTML.Attribute.ID));
					}
				}
			}
		} else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
			tooltip = editor.getToolTipText();
			Element elem = e.getSourceElement();
			if (elem != null) {
				AttributeSet attr = elem.getAttributes();
				AttributeSet a = (AttributeSet) attr.getAttribute(HTML.Tag.A);
				if (a != null && a.getAttribute(HTML.Attribute.TITLE) != null) {
					String htmlToolTip = "<html><body>" + ((String) a.getAttribute(HTML.Attribute.TITLE)).replaceAll("\n", "<br/>") + "</body></html>";
					editor.setToolTipText(htmlToolTip);
				}
			}
		} else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
			editor.setToolTipText(tooltip);
		}
	}

	public static String makeHyperLink(String href, String text, String title, String id, String style)
	{
		String attr = "";
		if(title != null) {
			attr += String.format(" title=\"%s\"", title);
		}
		if(id != null) {
			attr += String.format(" id=\"%s\"", id);
		}
		if(style != null) {
			attr += String.format(" style=\"%s\"", style);
		}
		return String.format("<a href=\"%s\"%s>%s</a>", href, attr, text);
	}

	private boolean objEquals(Object a, Object b) {
		return ((a == null && b == null) || (a != null && a.equals(b)));
	}

	// refer to https://stackoverflow.com/questions/46023177/set-inline-text-and-image-in-a-jeditorpane
	class ImgBaselineHTMLEditorKit extends HTMLEditorKit {
		private static final long serialVersionUID = 3268128657810856489L;

		@Override public ViewFactory getViewFactory() {
			return new HTMLEditorKit.HTMLFactory() {
				@Override public View create(Element elem) {
					//if (view instanceof LabelView) {
						//System.out.println(view.getAlignment(View.Y_AXIS));
					//}
					AttributeSet attrs = elem.getAttributes();
					Object elementName = attrs.getAttribute(AbstractDocument.ElementNameAttribute);
					Object o = elementName != null ? null : attrs.getAttribute(StyleConstants.NameAttribute);
					if (o instanceof HTML.Tag) {
						HTML.Tag kind = (HTML.Tag) o;
						if (kind == HTML.Tag.IMG) {
							return new BASE64ImageView(elem) {
								@Override public float getAlignment(int axis) {
									switch (axis) {
									case View.Y_AXIS:
										return .8125f; // magic number...
									default:
										return super.getAlignment(axis);
									}
								}
							};
						}
					}
					return super.create(elem);
				}
			};
		}
	}
}
