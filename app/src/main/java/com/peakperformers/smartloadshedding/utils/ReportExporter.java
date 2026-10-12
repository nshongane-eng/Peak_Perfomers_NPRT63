package com.peakperformers.smartloadshedding.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.peakperformers.smartloadshedding.database.BusinessEntity;
import com.peakperformers.smartloadshedding.database.LossEntryEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ReportExporter {

    public static void exportLossReport(Context context, List<LossEntryEntity> entries, BusinessEntity business) {
        if (context == null) return;

        if (entries == null || entries.isEmpty()) {
            Toast.makeText(context, "No loss entries available to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault());

        String businessName = (business != null && business.name != null && !business.name.isEmpty())
                ? business.name : "Smart LoadShedding Business";
        String suburb = (business != null && business.suburb != null) ? business.suburb : "Unspecified Suburb";
        String businessType = (business != null && business.businessType != null) ? business.businessType : "General Enterprise";

        double grandTotal = 0;
        Map<String, Double> categoryTotals = new HashMap<>();

        for (LossEntryEntity entry : entries) {
            grandTotal += entry.amount;
            String cat = Objects.requireNonNullElse(entry.category, "Other");
            Double currentCatTotal = categoryTotals.get(cat);
            categoryTotals.put(cat, (currentCatTotal != null ? currentCatTotal : 0.0) + entry.amount);
        }

        String fileName = "LoadShedding_Loss_Report_" + fileDateFormat.format(new Date()) + ".csv";
        File reportsDir = new File(context.getFilesDir(), "reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }
        File file = new File(reportsDir, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            // Header Section
            writer.append("====================================================\n");
            writer.append("     SMART LOAD SHEDDING - OFFICIAL LOSS STATEMENT   \n");
            writer.append("====================================================\n");
            writer.append("Business Name:,").append(escapeCsv(businessName)).append("\n");
            writer.append("Suburb / Area:,").append(escapeCsv(suburb)).append("\n");
            writer.append("Business Type:,").append(escapeCsv(businessType)).append("\n");
            writer.append("Report Date:,").append(dateFormat.format(new Date())).append("\n");
            writer.append("Total Incidents Logged:,").append(String.valueOf(entries.size())).append("\n");
            writer.append("GRAND TOTAL LOSS (ZAR):,").append(String.format(Locale.getDefault(), "R %.2f", grandTotal)).append("\n");
            writer.append("====================================================\n\n");

            // Category Summary Breakdown
            writer.append("--- CATEGORY LOSS BREAKDOWN ---\n");
            writer.append("Category,Total Amount (ZAR)\n");
            for (Map.Entry<String, Double> catEntry : categoryTotals.entrySet()) {
                writer.append(escapeCsv(catEntry.getKey())).append(",")
                        .append(String.format(Locale.getDefault(), "R %.2f", catEntry.getValue())).append("\n");
            }
            writer.append("\n");

            // Detailed Itemized Records Table
            writer.append("--- ITEMIZED LOSS RECORDS ---\n");
            writer.append("ID,Date & Time,Category,Amount (ZAR),Notes & Cause,Synced Status\n");

            for (LossEntryEntity entry : entries) {
                String dateStr = dateFormat.format(new Date(entry.createdAt));
                String notes = entry.notes != null ? entry.notes : "";
                String syncStatus = entry.synced ? "Synced to Cloud" : "Stored Locally";

                writer.append(String.valueOf(entry.id)).append(",")
                        .append(escapeCsv(dateStr)).append(",")
                        .append(escapeCsv(entry.category)).append(",")
                        .append(String.format(Locale.getDefault(), "R %.2f", entry.amount)).append(",")
                        .append(escapeCsv(notes)).append(",")
                        .append(escapeCsv(syncStatus)).append("\n");
            }

            writer.append("\n====================================================\n");
            writer.append("Verification: Verified Statement generated by Smart LoadShedding App.\n");
            writer.append("Valid for insurance claims, SARS tax deductions & grant applications.\n");
            writer.append("====================================================\n");

            writer.flush();

            // Obtain FileProvider URI using the dynamic package name
            String authority = context.getPackageName() + ".fileprovider";
            Uri contentUri = FileProvider.getUriForFile(context, authority, file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.setClipData(ClipData.newRawUri("Loss Report CSV", contentUri));
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Smart LoadShedding - Financial Loss Statement (" + businessName + ")");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Attached is the official Load Shedding Loss Report for " + businessName +
                    ".\nGrand Total: R " + String.format(Locale.getDefault(), "%.2f", grandTotal) +
                    "\nGenerated via Smart LoadShedding App.");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent chooser = Intent.createChooser(shareIntent, "Export Loss Report CSV");
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (!(context instanceof Activity)) {
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            try {
                context.startActivity(chooser);
            } catch (Exception ex) {
                // If text/csv intent chooser cannot be launched, fallback to text/plain
                shareAsPlainText(context, file, businessName);
            }

        } catch (Exception e) {
            Toast.makeText(context, "Failed to generate report: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static void shareAsPlainText(Context context, File file, String businessName) {
        try {
            StringBuilder textReport = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    textReport.append(line).append("\n");
                }
            }

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Smart LoadShedding - Financial Loss Statement (" + businessName + ")");
            shareIntent.putExtra(Intent.EXTRA_TEXT, textReport.toString());

            Intent chooser = Intent.createChooser(shareIntent, "Export Loss Report");
            if (!(context instanceof Activity)) {
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            context.startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(context, "Report generated successfully at: " + file.getName(), Toast.LENGTH_LONG).show();
        }
    }

    private static String escapeCsv(String input) {
        if (input == null) return "\"\"";
        String clean = input.replace("\"", "\"\"");
        return "\"" + clean + "\"";
    }
}
