package org.delcom.ifs25044pbo2026springbootcashflow.framework;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;

/**
 * Konfigurasi halaman global (app shell): metadata SEO, ikon, dan tema warna.
 * Berada di layer framework karena bergantung pada Vaadin.
 */
public class AppShell implements AppShellConfigurator {

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.addMetaTag("description",
                "Aplikasi web pencatat keuangan pribadi: catat pemasukan dan pengeluaran, "
                        + "cari dan urutkan transaksi, serta pantau saldo Anda.");
        settings.addMetaTag("color-scheme", "light");
        settings.addMetaTag("theme-color", "#1E3A8A");
        settings.addFavIcon("icon", "icons/favicon.svg", "any");
    }
}
