//
// This file is part of Corina.
// 
// Corina is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// Corina is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with Corina; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// Copyright 2001 Ken Harris <kbh7@cornell.edu>
//

package corina.cross;

import corina.gui.XFrame;
import corina.gui.PrintableDocument;
import corina.gui.FileDialog;
import corina.gui.UserCancelledException;

import java.io.IOException;

import java.util.List;

import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PageFormat;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

public class TableFrame extends XFrame implements PrintableDocument {

    // gui
    private JTable table;

    // data
    private String s=null;
    private List ss=null;
    private Table t=null;

    // toString -- ??
    public String toString() {
	// ouch, toString might get called before t is initialized.
	if (t == null)
	    return "???";

	// normal return statement here
	return t.toString();
    }

    private void computeTable() {
	try {
	    t = new Table(s, ss);
	    setTitle(t.toString());
	} catch (IOException ioe) {
	    // t = null;
	    return;
	}
	t.run();
    }

    private void initTable() {
	table = new JTable(new TableTableModel(t));
	getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public int getPrintingMethod() {
        return PrintableDocument.PAGEABLE;
    }
    public Pageable makePageable(PageFormat pf) {
	return new TablePager(t, pf);
    }
    public Printable makePrintable(PageFormat pf) {
        return null;
    }
    public String getPrintTitle() {
        return "Table"; // customize with master?
    }

    private void initButtons() {
        JPanel tmpPanel = new JPanel();
        tmpPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 0, 6, 6));
        tmpPanel.add(buttonPanel);
        getContentPane().add(tmpPanel, BorderLayout.SOUTH);

        // classloader, for icons
        ClassLoader cl = this.getClass().getClassLoader();

        JButton saveButton = new JButton("Save (ASCII)...",
                                         new ImageIcon(cl.getResource("toolbarButtonGraphics/general/Save16.gif")));
        saveButton.setMnemonic('S');
        saveButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    t.saveText(FileDialog.showSingle("Save table"));
                } catch (IOException ioe) {
                    JOptionPane.showMessageDialog(null,
                                                  "Error while saving: " + ioe.getMessage(),
                                                  "Error saving",
                                                  JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (UserCancelledException uce) {
                    // ignore
                }
            }
        });
        buttonPanel.add(saveButton);

        JButton saveButton2 = new JButton("Save (HTML)...",
                                          new ImageIcon(cl.getResource("toolbarButtonGraphics/general/Save16.gif")));
        saveButton2.setMnemonic('H');
        saveButton2.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    t.saveHTML(FileDialog.showSingle("Save table"));
                } catch (IOException ioe) {
                    JOptionPane.showMessageDialog(null,
                                                  "Error while saving: " + ioe.getMessage(),
                                                  "Error saving",
                                                  JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (UserCancelledException uce) {
                    // ignore
                }
            }
        });
        buttonPanel.add(saveButton2);
    }

    private void create() {
	// create frame
	setTitle("Table");
	setDefaultCloseOperation(DISPOSE_ON_CLOSE);

	// init gui
	computeTable();
	initTable();
	initButtons();

	// show the frame
	pack();
	setSize(new Dimension(640, 480));
	show();
    }

    public TableFrame(String s, List ss) {
        // data
        this.s = s;
        this.ss = ss;
        create(); // pseudo-constructor
    }

    public TableFrame() {
        try {
            s = FileDialog.showSingle("First");
            ss = FileDialog.showMulti("Samples");
            create();
        } catch (UserCancelledException uce) {
            dispose(); // ok?
            return;
        }
    }
}
