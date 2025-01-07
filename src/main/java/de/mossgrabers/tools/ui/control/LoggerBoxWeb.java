// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2019-2024
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.tools.ui.control;

import de.mossgrabers.tools.StringUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javafx.application.Platform;
import javafx.scene.effect.BlendMode;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * A window for logging information and error messages. Uses a web view component.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class LoggerBoxWeb
{
    private static final String [] EMPTY_LINE     = new String []
    {
        ""
    };

    private static final String    EMPTY_DOCUMENT = """
            <html>
                <head>
                    <meta charset=utf-8>
                    <style>
                    DIV, SPAN { font-family: %s; font-size: %s; color: %s; }
                    .error { color: red; }
                    </style>
                </head>
                <body>
                    <div id="content"></div>
                </body>
            </html>
            """;

    private final WebView          webView        = new WebView ();
    private final WebEngine        engine;

    private String                 fontFamily     = "\"Lucida Console\", \"Courier New\", monospace";
    private String                 fontSize       = "16pt";
    private String                 fontColor      = "black";
    private List<Node>             children       = new ArrayList<> ();
    private final int              clearMax;

    private Element                contentElement;


    /**
     * Constructor.
     * 
     * @param clearMax The maximum number of elements allowed to add before a clear happens
     */
    public LoggerBoxWeb (final int clearMax)
    {
        this.clearMax = clearMax;
        this.engine = this.webView.getEngine ();
        this.webView.setBlendMode (BlendMode.OVERLAY);
        this.clear ();
    }


    /**
     * Set the font family.
     *
     * @param fontFamily The font family
     */
    public void setFontFamily (final String fontFamily)
    {
        this.fontFamily = fontFamily;
    }


    /**
     * Set the font size.
     *
     * @param fontSize The font size
     */
    public void setFontSize (final String fontSize)
    {
        this.fontSize = fontSize;
    }


    /**
     * Set the font color.
     *
     * @param color The font color
     */
    public void setColor (final String color)
    {
        this.fontColor = color;
    }


    /**
     * Display a notification.
     *
     * @param message The message to display
     */
    public void notify (final String message)
    {
        this.append (message, false);
    }


    /**
     * Display an error message.
     *
     * @param message The message to display
     */
    public void notifyError (final String message)
    {
        this.append (message, true);
    }


    /**
     * Display an error notification.
     *
     * @param message The message to display
     * @param throwable The throwable to log
     */
    public void notifyError (final String message, final Throwable throwable)
    {
        final StringBuilder sb = new StringBuilder (message).append ('\n');
        final StringWriter sw = new StringWriter ();
        final PrintWriter pw = new PrintWriter (sw);
        throwable.printStackTrace (pw);
        sb.append (sw.toString ()).append ('\n');
        this.append (sb.toString (), true);
    }


    /**
     * Appends the text to the result text area.
     *
     * @param text The text to append
     * @param isError If true the text is highlighted in red
     */
    private void append (final String text, final boolean isError)
    {
        final String [] lines;
        if ("\n".equals (text))
            lines = EMPTY_LINE;
        else
            lines = StringUtils.split (text, "\n");
        this.append (lines, text.endsWith ("\n"), isError);
    }


    /**
     * Appends the text to the result text area.
     *
     * @param lines The lines of text to append
     * @param finalReturn True if the last line should have a return as well
     * @param isError If true the text is highlighted in red
     */
    private void append (final String [] lines, final boolean finalReturn, final boolean isError)
    {
        final Document doc = this.engine.getDocument ();
        if (doc == null)
            return;
        synchronized (this.children)
        {
            for (int i = 0; i < lines.length; i++)
            {
                final Text lineText = doc.createTextNode (lines[i]);
                if (isError)
                {
                    final Element errSpan = doc.createElement ("span");
                    errSpan.setAttribute ("class", "error");
                    errSpan.appendChild (lineText);
                    this.children.add (errSpan);
                }
                else
                {
                    this.children.add (lineText);
                }

                if (i + 1 < lines.length || finalReturn)
                {
                    final Element brElement = doc.createElement ("br");
                    this.children.add (brElement);
                }
            }
        }

        Platform.runLater ( () -> {
            synchronized (this.engine)
            {
                final Element el = this.getContentElement ();
                if (el == null)
                    return;
                synchronized (this.children)
                {
                    for (final Node child: this.children)
                        el.appendChild (child);
                    this.children.clear ();
                }

                // Limit the number of elements to prevent a crash
                if (this.clearMax > 0 && el.getChildNodes ().getLength () > this.clearMax)
                    this.clearInternal ();
                else
                    this.engine.executeScript ("window.scrollTo(0,document.body.scrollHeight);");
            }
        });
    }


    /**
     * Clears the logged text.
     */
    public final void clear ()
    {
        Platform.runLater ( () -> {
            synchronized (this.engine)
            {
                this.clearInternal ();
            }
        });
    }


    private void clearInternal ()
    {
        this.contentElement = null;
        this.engine.loadContent (String.format (EMPTY_DOCUMENT, this.fontFamily, this.fontSize, this.fontColor));
    }


    private Element getContentElement ()
    {
        if (this.contentElement == null)
        {
            final Document doc = this.engine.getDocument ();
            if (doc == null)
                return null;
            this.contentElement = doc.getElementById ("content");
        }
        return this.contentElement;
    }


    /**
     * Get the web view.
     *
     * @return The web view
     */
    public WebView getComponent ()
    {
        return this.webView;
    }
}
