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

import corina.Sample;
import corina.Element;
import corina.cross.Score;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.text.DecimalFormat;
import java.text.MessageFormat;

import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
   A crossdating table: a table of samples, crossdates, and other
   information, from an 1-by-N cross.<p>

   A sample table might look similar to this when printed:<p>

   <table border="0" padding="0" spacing="0" bgcolor="lightblue">
   <tr><td>

   <h3>Crossdating table for: Zonguldak, Karabuk Spring 99 Master</h3>
   <table border="1">
	<tr>
		<th>Sample</th>
		<th>T-Score</th>
		<th>Trend</th>
		<th>D-Score</th>
		<th>Overlap</th>
		<th>Distance</th>
	</tr>
	<tr>
		<td>Istanbul, Belgrade Forest</td>
		<td>2.36</td>
		<td>76.5%</td>
		<td>0.62</td>
		<td>n=136</td>
		<td>49 km</td>
	</tr>
   </table>

   </tr></td>
   </table>

   @author <a href="mailto:kbh7@cornell.edu">Ken Harris</a>
   @version $Id$ */

public class Table implements Runnable { //, Printable, Pageable {

    // input
    public Sample singleton; // temporarily (?) public
    private List ss;

    // i18n
    private static ResourceBundle msg = ResourceBundle.getBundle("TextBundle");

    // get rid of these formatters eventually.  scores should be able
    // to format themselves.
    static DecimalFormat f1, f2, f3;
    static {
        // REFACTOR: the crosses to use should be user-pickable, so this is B-A-D.
        f1 = new DecimalFormat(new TScore().getFormat());
        f2 = new DecimalFormat(new Trend().getFormat());
        f3 = new DecimalFormat(new DScore().getFormat());
    }

    // output
    List table; // of Table.Row // PUBLIC!

    /**
       A row of the 1-by-N table, which holds the sample's title,
       crossdating scores, and other information.

       @author <a href="mailto:kbh7@cornell.edu">Ken Harris</a>
       @version $Id$ */
    public class Row {
        /** The row's (sample's) title. */
        public String title;

	// crossdate scores, overlap, distance
	Single cross;

	public Row(Sample fixed, Sample moving) {
	    title = moving.toString();
	    cross = new Single(fixed, moving); // here's where all the computations get done
	}

        // write the row, given a format string
	// {0}=title, {1}-{3}=t/tr/d, {4}=overlap, {5}=dist
        public String format(MessageFormat format) {
	    return format.format(new Object[] {
		title,
		f1.format(cross.t), f2.format(cross.tr), f3.format(cross.d),
		String.valueOf(cross.n),
		cross.distanceAsString(),
	    });
        }
    }

    /** Construct a new Table.
	@param s Sample to compare all others to
	@param ss List of Elements to make table rows from
	@exception IOException if the sample <code>s</code> cannot be loaded
        @deprecated */
    public Table(String s, List ss) throws IOException {
        singleton = new Sample(s);
        this.ss = ss;
        table = new ArrayList();
    }

    public Table(Sequence seq) throws IOException {
        // what to do?
        // -- set singleton -- first fixed sample
        String fixed = (String) seq.getAllFixed().get(0); // use current view?
        singleton = new Sample(fixed);
        // -- set ss -- all moving samples (excluding the fixed one)
        ss = new ArrayList();
        for (int i=0; i<seq.getAllMoving().size(); i++) {
            String moving = (String) seq.getAllMoving().get(i);
            if (!fixed.equals(moving))
                ss.add(new Element(moving));
        }
        // -- create table list
        table = new ArrayList();
        // -- compute table?  no, not yet -- run() computes table
        run(); // no, do this later!
        // hmm...
        // -- update table on-screen as it's computed, like browser does
        // -- need a clever way to handle ioexceptions in run() -- dummy 'breakage' element singleton?
        // how?
        // -- sequence needs getfixed(), getmoving() methods
    }
    
    /** The title of this table.  The format is <code>"Crossdating
        Table for sampleTitle"</code> (for the title
        <code>sampleTitle</code> of the singleton).
	@return the title of this table */
    public String toString() {
	return msg.getString("table") + ": " + singleton.toString();
        // should be: "Crossdate: table for blah (range)"
    }

    /** Run all computations for the table.  Computes T, trend, D,
	overlap, and distance (if applicable) between sites.  */
    public void run() {
        for (int i=0; i<ss.size(); i++) {
            // skip inactive elements
            if (!((Element) ss.get(i)).isActive())
                continue;

            // load element into s2
            String f = ((Element) ss.get(i)).filename;
            Sample s2; // "s2"?  geez, what was i on?
            try {
                s2 = new Sample(f);
            } catch (IOException ioe) {
                continue; // ugh-ly!
            }

            // add new row to table
            table.add(new Row(singleton, s2));
        }
    }

    // true, this [saveHTML] is simple enough it shouldn't be in its own class.  therefore, for exportdialog,
    // i need to be able to REGISTER THIS METHOD as a saver for this type.  exportdialog, then,
    // is given any object and presents any savers it knows about:

    // [[ gee, i really wish i had closures.  but i think i've said that before... ]]

    /*
     static {
         ExportDialog.register(Sample.class, "Tucson.save()");
         ExportDialog.register(Table.class, "Table.saveHTML()"); // how does it get the name of this format, then?
         (duh, tell it)
             ExportDialog.register(Table.class, "Table.saveHTML()", "HTML");
     }
     ...
     and then to use, simply:
     ExportDialog.export(myTable); -- does myTable.getClass(), checks its registry, lists "Text" and "HTML"
     ...
     ALSO: i should be able to simply declare a "default" filetype, so file->save doesn't need any special code.
     ExportDialog.register(Sample.class, "Corina.save()", true);
     -- what if the format depends on the view, like cross/table/grid?  (duh, it shouldn't)

     ...
     -- exporters for crossdates are "HTML (x)", for x = { sigs, all, histo, table, grid }
     */
    
    // save as HTML -- XHTML 1.1, in fact
    public void saveHTML(String filename) throws IOException {
        // open file, etc.
        BufferedWriter w = new BufferedWriter(new FileWriter(filename));

        // write header.  newlines don't really do anything in HTML,
        // and browsers are used to dealing with all sorts of crazy
        // newlines, so i'll just use \n and be concise instead of
        // w.newLine().

       // XHTML1.1 -- this one's from http://www.alistapart.com/stories/doctype/
        w.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" " +
                "\"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">");

        w.write("<html>\n");
        w.write("\n");
        w.write("<head>\n");
        w.write("   <title>" + toString() + "</title>\n");
        w.write("   <style>\n");
        w.write("      <!--\n");
        w.write("         td, th { text-align: left }\n");
        w.write("         tr.odd { background-color: #eef }\n");
        w.write("         tr.even { background-color: #fff }\n");
        w.write("      -->\n");
        w.write("   </style>\n");
        w.write("</head>\n");
        w.write("\n");
        w.write("<body>\n");
        w.write("\n");
        w.write("<table border=\"0\" cellspacing=\"4\" cellpadding=\"4\" rules=\"groups\">\n");
        w.write("   <caption>" + toString() + "</caption>\n");
        w.write("\n");
        w.write("   <colgroup align=\"left\" span=\"2\"/>\n");
        w.write("   <colgroup align=\"left\"/>\n");
        w.write("   <colgroup align=\"left\"/>\n");
        w.write("   <colgroup align=\"left\"/>\n");
        w.write("   <colgroup align=\"left\"/>\n");
        w.write("   <colgroup align=\"left\"/>\n");
        w.write("\n");
        w.write("   <thead>\n");
        w.write("   <tr>\n");
        w.write("      <th width=\"40%\">" + msg.getString("title") + "</th>\n");
        w.write("      <th width=\"10%\">" + msg.getString("range") + "</th>\n");
        w.write("      <th width=\"10%\">" + msg.getString("tscore") + "</th>\n");
        w.write("      <th width=\"10%\">" + msg.getString("trend") + "</th>\n");
        w.write("      <th width=\"10%\">" + msg.getString("dscore") + "</th>\n");
        w.write("      <th width=\"10%\">" + msg.getString("overlap") + "</th>\n");
        w.write("      <th width=\"10%\">" + msg.getString("distance") + "</th>\n");
        w.write("   </tr>\n");
        w.write("\n");
        w.write("   <tbody>\n");

        // write lines
	MessageFormat html = new MessageFormat("<td>{0}</td><td>{1}</td><td>{2}</td>" +
					       "<td>{3}</td><td>{4}</td><td>{5}</td>");
        for (int i=0; i<table.size(); i++) {
            // get table row
            Table.Row r = (Table.Row) table.get(i);

            // write html table row
            w.write("   <tr class=\"" + (i%2==1 ? "odd" : "even") + "\">\n");
	    w.write(r.format(html));
            w.write("   </tr>\n");
        }

        w.write("   </tbody>\n");
        w.write("</table>\n");
        w.write("\n");
        w.write("</body>\n");
        w.write("\n");
        w.write("</html>\n");

        // close file
        w.close();
    }

    // save as plaintext -- for spreadsheets, text-only (dot-matrix) printers,
    // or for inserting into a table (in a word processor) for your own formatting.
    public void saveText(String filename) throws IOException {
        // open (buffered) file
        BufferedWriter w = new BufferedWriter(new FileWriter(filename));

        // write header
        w.write(msg.getString("title") + "\t" +
		msg.getString("tscore") + "\t" +
                msg.getString("trend") + "\t" +
                msg.getString("dscore") + "\t" +
                msg.getString("overlap") + "\t" +
                msg.getString("distance"));
        w.newLine();

        // write lines
	MessageFormat text = new MessageFormat("{0}\t{1}\t{2}\t{3}\t{4}\t{5}");
        for (int i=0; i<table.size(); i++) {
            Table.Row r = (Table.Row) table.get(i);
	    w.write(r.format(text));
            w.newLine(); // (make me part of the MF?)
        }

	// (loop for r in table do (write (row-format r text))) -- or something like that

        // close file
        w.close();
    }
}
