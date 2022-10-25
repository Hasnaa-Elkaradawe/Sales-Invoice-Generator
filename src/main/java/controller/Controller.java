
package controller;


import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import model.InvoiceHeader;
import model.InvoiceHeaderTableModel;
import model.InvoiceLine;
import model.InvoiceLineTableModel;
import view.CreateNewInvoice;
import view.CreateNewLine;
import view.InvoiceFrame;

/**
 *
 * @author helkaradawe
 */
public class Controller {
    
     JFileChooser invoice_loader = new JFileChooser();
    JFileChooser invoice_lines_loader = new JFileChooser();

    int invoices_col_count = 3;
    int lines_col_count = 4;
    int max_id = 0;

    ArrayList<InvoiceHeader> invoices;
    ArrayList<InvoiceLine> invoices_lines;

    InvoiceFrame frame;
    
     private InvoiceHeaderTableModel store_invoice_header_table_model;
    private InvoiceLineTableModel store_invoice_line_table_model;
    public static SimpleDateFormat date_formatter = new SimpleDateFormat("dd-MM-yyyy");
     

   

    int selected_invoice_index = 0;
    int selected_line_index = 0;

    public Controller(InvoiceFrame frame) {
        this.frame = frame;
        invoices = new ArrayList<>();
        invoices_lines = new ArrayList<>();
        this.fillTables("InvoiceHeader.csv", "InvoiceLine.csv");
    
    }
    

    public void loadInvoicesData(String header_file_name) throws Exception, ParseException, IOException {
        File header_file = null;
        if (header_file_name != null) {
            header_file = new File(header_file_name);
        } else {
            int result = invoice_loader.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                header_file = invoice_loader.getSelectedFile();
            }
        }

        if (!".csv".equals((header_file.toString()).substring((header_file.toString()).lastIndexOf(".")))) {
            throw new Exception();
        } else {
            FileInputStream fls = null;
            List<String> lines = Files.lines(Paths.get(header_file.getAbsolutePath())).collect(Collectors.toList());
            String[][] data = new String[lines.size()][invoices_col_count];

            for (int i = 0; i < lines.size(); i++) {
                data[i] = lines.get(i).split(",");
            }

            for (int i = 0; i < data.length; i++) {
                invoices.add(new InvoiceHeader(
                        Integer.parseInt(data[i][0]),
                        this.date_formatter.parse(data[i][1]),
                        data[i][2]
                ));
            }
            max_id = Integer.parseInt(data[lines.size() - 1][0]);
            if (fls != null) {
                fls.close();
            }
        }
    }

    public void loadInvoicesLinesData(String lines_file_name) throws Exception, IOException {
        File lines_file = null;
        if (lines_file_name != null) {
            lines_file = new File(lines_file_name);
        } else {
            int result = invoice_loader.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                lines_file = invoice_loader.getSelectedFile();
            }
        }

        if (!".csv".equals((lines_file.toString()).substring((lines_file.toString()).lastIndexOf(".")))) {
            throw new Exception();
        } else {
            FileInputStream fls = null;
            List<String> lines = Files.lines(Paths.get(lines_file.getAbsolutePath())).collect(Collectors.toList());
            String[][] data = new String[lines.size()][lines_col_count];
            for (int i = 0; i < lines.size(); i++) {
                data[i] = lines.get(i).split(",");
            }

            for (int i = 0; i < data.length; i++) {
                InvoiceHeader invoice = null;
                for (int j = 0; j < invoices.size() && invoice == null; j++) {
                    if (invoices.get(j).getNum() == Integer.parseInt(data[i][0])) {
                        invoice = invoices.get(j);
                    }
                }
                if (invoice != null) {
                    invoices_lines.add(new InvoiceLine(
                            data[i][1],
                            Integer.parseInt(data[i][3]),
                            Double.parseDouble(data[i][2]),
                            invoice
                    ));
                }
            }

            for (InvoiceHeader invoice : invoices) {
                ArrayList<InvoiceLine> filtered_invoice_lines = new ArrayList<>();
                for (InvoiceLine invoices_line : invoices_lines) {
                    if (invoices_line.getInvoice_header().getNum() == invoice.getNum()) {
                        filtered_invoice_lines.add(invoices_line);
                    }
                }
                invoice.setLines(filtered_invoice_lines);
            }
            if (fls != null) {
                fls.close();
            }
        }
    }

    public void loadData(String header_file, String lines_file) throws Exception {
        invoices = new ArrayList<>();
        invoices_lines = new ArrayList<>();
        try {
            loadInvoicesData(header_file);
            loadInvoicesLinesData(lines_file);
            this.log();
        } catch (IOException io) {
            JOptionPane.showMessageDialog(this.frame, "File not found, make sure the file you selected still exists", "File not found", JOptionPane.ERROR_MESSAGE);
            throw new Exception();
        } catch (ParseException pe) {
            JOptionPane.showMessageDialog(this.frame, "Wrong date format, should be dd-MM-yyyy ex: 19-04-1963", "Wrong date format", JOptionPane.ERROR_MESSAGE);
            throw new Exception();
           
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this.frame, "Wrong file format, validate data and make sure loaded file is csv", "Wrong file format", JOptionPane.ERROR_MESSAGE);
            throw new Exception();
        }
    }

    public InvoiceHeaderTableModel getInvoiceHeaderModel() throws Exception {
        return new InvoiceHeaderTableModel(invoices);
    }

    public InvoiceLineTableModel getInvoiceLineModel() throws Exception {
        return new InvoiceLineTableModel(!invoices.isEmpty() ? invoices.get(0).getLines() : new ArrayList<InvoiceLine>());
    }

    private void log() {
        String tab = "  ";
        System.out.println("-----------Logging loaded data start-----------");
        System.out.println("");
        for (InvoiceHeader invoice : invoices) {
            System.out.println("Invoice num: " + invoice.getNum());
            System.out.println("{");
            System.out.println(tab + "Date: " + this.date_formatter.format(invoice.getDate()) + ",  Customer name: " + invoice.getName());
            System.out.println(tab + "Items: [");
            for (InvoiceLine invoice_line : invoice.getLines()) {
                System.out.println(tab + tab + "Item name: " + invoice_line.getName() + ",  Price: " + invoice_line.getPrice() + ", Count: " + invoice_line.getCount());
            }
            System.out.println(tab + "]");
            System.out.println("}");
            System.out.println("");
        }
        System.out.println("-----------Logging loaded data end-----------");
    }
    


    public void fillTables(String header_file_path, String line_file_path) {
        try {
            this.loadData(header_file_path, line_file_path);

            store_invoice_header_table_model = this.getInvoiceHeaderModel();
            store_invoice_line_table_model = this.getInvoiceLineModel();
            this.refreshTables();
            this.refreshData();

            max_id = this.max_id;
            this.autoSelectFirstInvoice();
        } catch (Exception e) {
            System.out.println("Error loading files");
        }
    }

    public void showCreateInvoice(ActionEvent e) {
        CreateNewInvoice create_invoice_form = new CreateNewInvoice(this);
        create_invoice_form.setVisible(true);
    }

    public void showCreateLine(ActionEvent e) {
        CreateNewLine create_Line_form = new CreateNewLine(this);
        create_Line_form.setVisible(true);
    }

    public void CancelCreateInvoice(ActionEvent evt, CreateNewInvoice create_invoice_frame) {
        create_invoice_frame.dispose();
    }

    public void CancelCreateLine(ActionEvent evt, CreateNewLine create_line_frame) {
        create_line_frame.dispose();
    }

    public void clickInvoiceTable(MouseEvent evt) {
        JTable invoices_table = (JTable) evt.getSource();
        Point point = evt.getPoint();
        int row = invoices_table.rowAtPoint(point);
        InvoiceHeaderTableModel invoices_table_model = (InvoiceHeaderTableModel) invoices_table.getModel();
        InvoiceHeader invoice_header = invoices_table_model.getInvoice_headers().get(row);
        ArrayList<InvoiceLine> invoice_lines = invoice_header.getLines();
        selected_invoice_index = row;

        store_invoice_line_table_model.setInvoice_lines(invoice_lines);
        //refresh line table only to keep highlight
        store_invoice_line_table_model.fireTableDataChanged();
        frame.getInvoiceLineTable().setModel(store_invoice_line_table_model);
        this.refreshData();
    }

    public void clickLineTable(MouseEvent evt) {
        JTable lines_table = (JTable) evt.getSource();
        Point point = evt.getPoint();
        int row = lines_table.rowAtPoint(point);
        selected_line_index = row;
    }

    public void OkCreateInvoice(ActionEvent evt, CreateNewInvoice create_invoice_frame, String customer_name, String invoice_date) {
        try {
            if (customer_name.isBlank()) {
                JOptionPane.showMessageDialog(this.frame, "All fields are required", "Wrong data format", JOptionPane.ERROR_MESSAGE);
            } else {
                Date date = date_formatter.parse(invoice_date);
                InvoiceHeader new_invoice = new InvoiceHeader(++max_id, date, customer_name);
                store_invoice_header_table_model.addInvoiceHeader(new_invoice);
                this.refreshTables();
                create_invoice_frame.dispose();

                if (store_invoice_header_table_model.getInvoice_headers().size() == 1) {
                    this.autoSelectFirstInvoice();
                }
            }
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this.frame, "Wrong date format, should be dd-MM-yyyy ex: 19-04-1963", "Wrong date format", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void OkCreateLine(ActionEvent evt, CreateNewLine create_new_line_frame, String item_name, String count_item, String price_item) {
        try {
            if (store_invoice_header_table_model.getInvoice_headers().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Can't add a new line when there are no headers first", "Can't create item", JOptionPane.ERROR_MESSAGE);
            } else if (count_item.isBlank() || price_item.isBlank() || item_name.isBlank()) {
                JOptionPane.showMessageDialog(this.frame, "All fields are required", "Wrong data format", JOptionPane.ERROR_MESSAGE);
            } else {
                InvoiceHeader selected_invoice = store_invoice_header_table_model.getInvoice_headers().get(selected_invoice_index);
                InvoiceLine new_line = new InvoiceLine(item_name, Integer.parseInt(count_item), Double.parseDouble(price_item), selected_invoice);

                store_invoice_line_table_model.addInvoiceLine(new_line);
                this.refreshTables();

                create_new_line_frame.dispose();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this.frame, "Wrong count and/or price format, must be a number/floating point number", "Wrong data format", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void deleteInvoice(ActionEvent evt) {
        if (store_invoice_header_table_model.getInvoice_headers().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Can't delete invoice header when table is empty", "Can't delete item", JOptionPane.ERROR_MESSAGE);
        } else {
            store_invoice_header_table_model.removeInvoiceHeader(selected_invoice_index);
            this.autoSelectFirstInvoice();
        }
    }

    public void deleteInvoiceLine(ActionEvent evt) {
        if (store_invoice_line_table_model.getInvoice_lines().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Can't delete invoice line when table is empty", "Can't delete item", JOptionPane.ERROR_MESSAGE);
        } else {
            store_invoice_line_table_model.removeInvoiceLine(selected_line_index);
            this.refreshTables();
            this.refreshData();
        }
    }

    public void save() {
        ArrayList<InvoiceHeader> invoice_headers = store_invoice_header_table_model.getInvoice_headers();
        ArrayList<InvoiceLine> invoice_lines = new ArrayList<>();
        for (InvoiceHeader invoice_header : invoice_headers) {
            for (InvoiceLine invoice_header_line : invoice_header.getLines()) {
                invoice_lines.add(invoice_header_line);
            }
        }
        String invoice_headers_data = InvoiceHeader.toCSV(invoice_headers);
        String invoice_lines_data = InvoiceLine.toCSV(invoice_lines);

        if (invoice_headers.isEmpty() || invoice_lines.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Invoice header and/or lines tables are empty", "Can't export files", JOptionPane.ERROR_MESSAGE);
        } else {
            System.out.println("-----------Logging exported data start-----------");
            System.out.println(invoice_headers_data);
            System.out.println(invoice_lines_data);
            System.out.println("-----------Logging exported data end-----------");
            JFileChooser fc = new JFileChooser();
            int result = fc.showSaveDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File header_file = fc.getSelectedFile();
                result = fc.showSaveDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File line_file = fc.getSelectedFile();

                    if (header_file.getName().equals(line_file.getName())) {
                        JOptionPane.showMessageDialog(frame, "Files names can't be the same", "Wrong file format", JOptionPane.ERROR_MESSAGE);
                    } else {
                        try {
                            if (!".csv".equals((header_file.getPath()).substring((header_file.getPath()).lastIndexOf(".")))) {
                                throw new Exception();
                            }
                            if (!".csv".equals((line_file.getPath()).substring((line_file.getPath()).lastIndexOf(".")))) {
                                throw new Exception();
                            }
                            FileWriter invoice_header_file_writer = new FileWriter(header_file);
                            invoice_header_file_writer.write(invoice_headers_data);
                            invoice_header_file_writer.flush();
                            invoice_header_file_writer.close();

                            FileWriter invoice_line_file_writer = new FileWriter(line_file);
                            invoice_line_file_writer.write(invoice_lines_data);
                            invoice_line_file_writer.flush();
                            invoice_line_file_writer.close();
                        } catch (IOException io) {
                            JOptionPane.showMessageDialog(frame, "File/Directory not found, make sure file name/format/location are in order", "Not found", JOptionPane.ERROR_MESSAGE);
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(frame, "File should be of type .csv", "Wrong file format", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        }
    }

    private void autoSelectFirstInvoice() {
        ArrayList<InvoiceHeader> invoice_headers = store_invoice_header_table_model.getInvoice_headers();
        ArrayList<InvoiceLine> invoice_lines = new ArrayList<>();
        if (invoice_headers.size() > 0) {
            InvoiceHeader invoice_header = invoice_headers.get(0);
            invoice_lines = invoice_header.getLines();
        }
        selected_invoice_index = 0;

        store_invoice_line_table_model.setInvoice_lines(invoice_lines);
        this.refreshTables();
        this.refreshData();
    }

    private void refreshTables() {
        store_invoice_header_table_model.fireTableDataChanged();
        frame.getInvoiceHeaderTable().setModel(store_invoice_header_table_model);
        store_invoice_line_table_model.fireTableDataChanged();
        frame.getInvoiceLineTable().setModel(store_invoice_line_table_model);
    }

    private void refreshData() {
        ArrayList<InvoiceHeader> invoice_headers = store_invoice_header_table_model.getInvoice_headers();
        if (invoice_headers.size() > 0) {
            InvoiceHeader invoice_header = invoice_headers.get(selected_invoice_index);

            frame.getInvoiceNumberLabel().setText(String.valueOf(invoice_header.getNum()));
            frame.getInvoiceDateLabel().setText(this.date_formatter.format(invoice_header.getDate()));
            frame.getInvoiceCustomerNameLabel().setText(invoice_header.getName());
            frame.getInvoiceTotalLabel().setText(String.valueOf(invoice_header.getTotalInvoices()));
        } else {
            frame.getInvoiceNumberLabel().setText("");
            frame.getInvoiceDateLabel().setText("");
            frame.getInvoiceCustomerNameLabel().setText("");
            frame.getInvoiceTotalLabel().setText("");
        }
    }
    
    
    
}
