/**
 * The UiTools module.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
module de.mossgrabers.uitools
{
    requires java.desktop;
    requires java.logging;
    requires transitive java.prefs;
    requires transitive java.xml;
    requires transitive javafx.controls;
    requires javafx.graphics;


    exports de.mossgrabers.tools;
    exports de.mossgrabers.tools.ui;
    exports de.mossgrabers.tools.ui.action;
    exports de.mossgrabers.tools.ui.control;
    exports de.mossgrabers.tools.ui.control.loggerbox;
    exports de.mossgrabers.tools.ui.panel;
}