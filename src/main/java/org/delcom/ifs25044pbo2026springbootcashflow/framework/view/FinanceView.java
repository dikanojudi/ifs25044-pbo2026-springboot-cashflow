package org.delcom.ifs25044pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Section;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25044pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25044pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25044pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25044pbo2026springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25044pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25044pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25044pbo2026springbootcashflow.usecase.FinanceUseCase;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Tampilan web Vaadin untuk pencatatan keuangan.
 *
 * <p>Aturan bisnis ada di {@link FinanceUseCase}; format teks ada di {@link FinancePresenter};
 * view hanya menyusun komponen dan menangani exception.
 *
 * <p>Catatan desain (aksesibilitas, SEO, performa):
 * <ul>
 *   <li>Struktur semantik: header, main, footer, satu H1, dan H2 per bagian.</li>
 *   <li>Seluruh pasangan warna teks/latar memenuhi kontras WCAG AA (minimal 4,5:1).</li>
 *   <li>Setiap input punya label terlihat; setiap tombol hapus punya nama yang spesifik.</li>
 *   <li>Makna tidak hanya lewat warna: ada badge teks, tanda minus, dan keterangan saldo.</li>
 *   <li>Responsif: kartu dan toolbar otomatis turun baris di layar sempit.</li>
 * </ul>
 */
@Route("")
@PageTitle("Catatan Keuangan | Cashflow")
public class FinanceView extends Div {

    // Palet warna (semua lolos kontras WCAG AA terhadap latar pasangannya).
    private static final String INK = "#111827";      // teks utama
    private static final String MUTED = "#374151";    // teks sekunder
    private static final String BORDER = "#D1D5DB";
    private static final String PAGE = "#F3F4F6";
    private static final String SURFACE = "#FFFFFF";
    private static final String NAVY = "#1E3A8A";
    private static final String GREEN = "#166534";
    private static final String RED = "#B91C1C";
    private static final String BLUE = "#1D4ED8";

    private static final String EMPTY_TITLE = "Belum ada transaksi!";
    private static final String EMPTY_HINT = "Mulai dengan menekan Tambah Pemasukan atau Tambah Pengeluaran.";
    private static final String NOT_FOUND_TITLE = "Transaksi tidak ditemukan!";
    private static final String NOT_FOUND_HINT = "Coba kata kunci lain, atau tekan Muat Ulang untuk melihat semua transaksi.";

    private final FinanceUseCase useCase;
    private final FinancePresenter presenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField("Cari keterangan");
    private final ComboBox<SortOption> sortBox = new ComboBox<>("Urutkan berdasarkan");
    private final Paragraph statusLabel = new Paragraph();

    private final Div emptyBox = new Div();
    private final Span emptyTitle = new Span(EMPTY_TITLE);
    private final Span emptyHint = new Span(EMPTY_HINT);

    private final Span countValue = new Span("0");
    private final Span incomeValue = new Span();
    private final Span expenseValue = new Span();
    private final Span balanceValue = new Span();
    private final Span balanceNote = new Span();

    public FinanceView(FinanceUseCase useCase, FinancePresenter presenter) {
        this.useCase = useCase;
        this.presenter = presenter;

        // Latar halaman penuh; isi dibatasi lebar agar nyaman dibaca.
        getStyle()
                .set("background", PAGE)
                .set("color", INK)
                .set("min-height", "100vh")
                .set("box-sizing", "border-box")
                .set("padding", "1rem");

        Div container = column("1.25rem");
        container.getStyle()
                .set("max-width", "1100px")
                .set("margin", "0 auto");

        configureGrid();
        configureEmptyBox();
        configureToolbarFields();

        container.add(buildHeader(), buildMain(), buildFooter());
        add(container);

        refreshAll();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();
        // Bahasa dokumen (aksesibilitas + SEO): halaman ini berbahasa Indonesia.
        ui.getPage().executeJs("document.documentElement.setAttribute('lang', 'id');");
        // Warna label/placeholder bawaan komponen dipastikan cukup kontras,
        // termasuk pada dialog yang dirender di luar view ini.
        ui.getElement().getStyle().set("--lumo-secondary-text-color", MUTED);
    }

    // ------------------------------------------------------------------
    // Struktur halaman: header, main, footer
    // ------------------------------------------------------------------

    private Header buildHeader() {
        H1 title = new H1("Catatan Keuangan");
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "1.9rem")
                .set("line-height", "1.2")
                .set("color", SURFACE);

        Paragraph subtitle = new Paragraph("Catat pemasukan dan pengeluaran, lalu pantau saldo Anda dengan mudah.");
        subtitle.getStyle()
                .set("margin", "0.4rem 0 0")
                .set("font-size", "1rem")
                .set("color", "#DBEAFE");

        Header header = new Header(title, subtitle);
        header.getStyle()
                .set("background", NAVY)
                .set("color", SURFACE)
                .set("border-radius", "14px")
                .set("padding", "1.4rem 1.5rem");
        return header;
    }

    private Main buildMain() {
        Main main = new Main(buildSummarySection(), buildTransactionSection());
        main.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "1.25rem");
        return main;
    }

    private Footer buildFooter() {
        Paragraph note = new Paragraph("Cashflow \u00B7 PBO 2026 \u00B7 Data disimpan sementara di memori "
                + "dan akan hilang saat aplikasi dijalankan ulang.");
        note.getStyle()
                .set("margin", "0")
                .set("font-size", "0.9rem")
                .set("color", MUTED)
                .set("text-align", "center");
        return new Footer(note);
    }

    // ------------------------------------------------------------------
    // Ringkasan (kartu)
    // ------------------------------------------------------------------

    private Section buildSummarySection() {
        Div cards = new Div(
                createCard("Total Transaksi", countValue, new Span("Jumlah transaksi tercatat"), BLUE),
                createCard("Total Pemasukan", incomeValue, new Span("Seluruh uang masuk"), GREEN),
                createCard("Total Pengeluaran", expenseValue, new Span("Seluruh uang keluar"), RED),
                createCard("Saldo", balanceValue, balanceNote, BLUE)
        );
        cards.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("gap", "0.9rem");

        return section("ringkasan-heading", "Ringkasan Keuangan", cards);
    }

    private Div createCard(String caption, Span value, Span note, String accent) {
        Span label = block(new Span(caption));
        label.getStyle()
                .set("font-size", "0.95rem")
                .set("font-weight", "600")
                .set("color", MUTED);

        block(value).getStyle()
                .set("margin", "0.3rem 0")
                .set("font-size", "1.55rem")
                .set("font-weight", "700")
                .set("color", accent)
                .set("overflow-wrap", "anywhere");

        block(note).getStyle()
                .set("font-size", "0.875rem")
                .set("color", MUTED);

        Div card = new Div(label, value, note);
        card.getStyle()
                .set("flex", "1 1 220px")
                .set("min-width", "0")
                .set("background", SURFACE)
                .set("color", INK)
                .set("border", "1px solid " + BORDER)
                .set("border-top", "5px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("box-sizing", "border-box");
        return card;
    }

    private void updateSummary() {
        BigDecimal balance = useCase.getBalance();
        countValue.setText(String.valueOf(useCase.getAllTransactions().size()));
        incomeValue.setText(presenter.formatMoney(useCase.getTotalIncome()));
        expenseValue.setText(presenter.formatMoney(useCase.getTotalExpense()));
        balanceValue.setText(presenter.formatMoney(balance));
        balanceValue.getStyle().set("color", balance.signum() < 0 ? RED : BLUE);
        balanceNote.setText(presenter.formatBalanceStatus(balance));
    }

    // ------------------------------------------------------------------
    // Daftar transaksi: toolbar + grid
    // ------------------------------------------------------------------

    private Section buildTransactionSection() {
        statusLabel.getElement().setAttribute("role", "status");
        statusLabel.getStyle()
                .set("margin", "0")
                .set("font-size", "0.95rem")
                .set("color", MUTED);

        Div body = column("0.9rem");
        body.add(buildToolbar(), statusLabel, grid, emptyBox);

        Div panel = new Div(body);
        panel.getStyle()
                .set("background", SURFACE)
                .set("border", "1px solid " + BORDER)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem");

        return section("transaksi-heading", "Daftar Transaksi", panel);
    }

    private void configureToolbarFields() {
        searchField.setPlaceholder("Contoh: gaji");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setMinWidth("14rem");

        sortBox.setPlaceholder("Pilih urutan");
        sortBox.setItems(SortOption.values());
        sortBox.setItemLabelGenerator(presenter::formatSortOption);
        sortBox.setClearButtonVisible(true);
        sortBox.setMinWidth("12rem");
        sortBox.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                sortTransactions(event.getValue());
            }
        });
    }

    private Div buildToolbar() {
        Button searchButton = secondaryButton("Cari", VaadinIcon.SEARCH, event -> searchTransactions());
        Button refreshButton = secondaryButton("Muat Ulang", VaadinIcon.REFRESH, event -> refreshAll());

        HorizontalLayout filters = new HorizontalLayout(searchField, searchButton, sortBox, refreshButton);
        filters.setPadding(false);
        filters.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        filters.getStyle().set("flex-wrap", "wrap");

        Button incomeButton = solidButton("Tambah Pemasukan", VaadinIcon.PLUS_CIRCLE, GREEN,
                event -> openFormDialog(TransactionType.INCOME));
        Button expenseButton = solidButton("Tambah Pengeluaran", VaadinIcon.MINUS_CIRCLE, RED,
                event -> openFormDialog(TransactionType.EXPENSE));

        HorizontalLayout actions = new HorizontalLayout(incomeButton, expenseButton);
        actions.setPadding(false);
        actions.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        actions.getStyle().set("flex-wrap", "wrap");

        Div toolbar = new Div(filters, actions);
        toolbar.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("justify-content", "space-between")
                .set("align-items", "flex-end")
                .set("gap", "0.75rem");
        return toolbar;
    }

    private void configureGrid() {
        grid.getElement().setAttribute("aria-label", "Tabel daftar transaksi");
        grid.setAllRowsVisible(true);

        grid.addColumn(Transaction::getId)
                .setHeader("ID")
                .setKey("id")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Transaction::getId);

        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan")
                .setKey("description")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));

        grid.addComponentColumn(this::createAmountLabel)
                .setHeader("Jumlah")
                .setKey("amount")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.END)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getAmount));

        grid.addComponentColumn(this::createTypeBadge)
                .setHeader("Tipe")
                .setKey("type")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getType));

        grid.addColumn(tx -> presenter.formatCreatedAt(tx))
                .setHeader("Waktu")
                .setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));

        grid.addComponentColumn(this::createDeleteButton)
                .setHeader("Tindakan")
                .setKey("actions")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(false);
    }

    private void configureEmptyBox() {
        block(emptyTitle).getStyle()
                .set("font-size", "1.1rem")
                .set("font-weight", "700")
                .set("color", INK);
        block(emptyHint).getStyle()
                .set("margin-top", "0.3rem")
                .set("color", MUTED);

        emptyBox.add(emptyTitle, emptyHint);
        emptyBox.getElement().setAttribute("role", "status");
        emptyBox.getStyle()
                .set("text-align", "center")
                .set("padding", "2rem 1rem")
                .set("border", "2px dashed " + BORDER)
                .set("border-radius", "12px")
                .set("background", SURFACE);
    }

    private Span createAmountLabel(Transaction tx) {
        Span label = new Span(presenter.formatMoney(tx.getAmount()));
        label.getStyle()
                .set("font-weight", "700")
                .set("color", tx.isIncome() ? GREEN : RED)
                .set("white-space", "nowrap");
        return label;
    }

    private Span createTypeBadge(Transaction tx) {
        Span badge = new Span(tx.getType().label());
        badge.getStyle()
                .set("display", "inline-block")
                .set("padding", "0.2rem 0.7rem")
                .set("border-radius", "999px")
                .set("font-size", "0.85rem")
                .set("font-weight", "600")
                .set("color", SURFACE)
                .set("background", tx.isIncome() ? GREEN : RED);
        return badge;
    }

    private Button createDeleteButton(Transaction tx) {
        Button button = outlineButton("Hapus", VaadinIcon.TRASH, RED, event -> openDeleteDialog(tx));
        // Nama yang spesifik untuk pembaca layar; diawali teks yang terlihat ("Hapus").
        button.getElement().setAttribute("aria-label",
                "Hapus transaksi " + tx.getId() + ": " + tx.getDescription());
        return button;
    }

    // ------------------------------------------------------------------
    // Dialog
    // ------------------------------------------------------------------

    private void openFormDialog(TransactionType type) {
        boolean income = type == TransactionType.INCOME;

        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setRequiredIndicatorVisible(true);
        descriptionField.setWidthFull();

        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setRequiredIndicatorVisible(true);
        amountField.setHelperText("Contoh: 1500000 atau 25000.50");
        amountField.setWidthFull();

        VerticalLayout form = new VerticalLayout(descriptionField, amountField);
        form.setPadding(false);

        Dialog dialog = new Dialog();
        Button saveButton = solidButton("Simpan", VaadinIcon.CHECK, income ? GREEN : RED, event -> {
            try {
                Transaction saved = income
                        ? useCase.addIncome(descriptionField.getValue(), amountField.getValue())
                        : useCase.addExpense(descriptionField.getValue(), amountField.getValue());
                showSuccess(presenter.showAddSuccess(saved));
                dialog.close();
                refreshAll();
            } catch (ValidationException ex) {
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        Button cancelButton = secondaryButton("Batal", VaadinIcon.CLOSE, event -> dialog.close());

        buildDialog(dialog, "Tambah " + type.label(), income ? GREEN : RED, form, cancelButton, saveButton);
        dialog.open();
    }

    private void openDeleteDialog(Transaction tx) {
        Paragraph message = new Paragraph("Yakin ingin menghapus transaksi \"" + tx.getDescription()
                + "\" (" + presenter.formatMoney(tx.getAmount()) + ")?");
        message.getStyle().set("margin", "0").set("color", INK);

        Dialog dialog = new Dialog();
        Button confirmButton = solidButton("Hapus", VaadinIcon.TRASH, RED, event -> {
            deleteTransaction(tx);
            dialog.close();
        });
        Button cancelButton = secondaryButton("Batal", VaadinIcon.CLOSE, event -> dialog.close());

        buildDialog(dialog, "Hapus Transaksi #" + tx.getId(), RED, message, cancelButton, confirmButton);
        dialog.open();
    }

    private void buildDialog(Dialog dialog, String title, String color, Component body, Button... buttons) {
        dialog.setWidth("460px");
        dialog.setMaxWidth("95vw");
        dialog.getElement().setAttribute("aria-label", title);

        H2 heading = new H2(title);
        heading.getStyle()
                .set("margin", "0 0 0.75rem")
                .set("font-size", "1.3rem")
                .set("color", color);

        HorizontalLayout footer = new HorizontalLayout(buttons);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.getStyle().set("margin-top", "1rem");

        dialog.add(heading, body, footer);
    }

    // ------------------------------------------------------------------
    // Aksi
    // ------------------------------------------------------------------

    private void deleteTransaction(Transaction tx) {
        try {
            useCase.removeTransaction(tx.getId());
            showSuccess(presenter.showRemoveSuccess());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
            refreshAll();
        } catch (ValidationException ex) {
            showError(presenter.showValidationError(ex.getMessage()));
        }
    }

    private void searchTransactions() {
        String keyword = searchField.getValue();
        if (keyword.isBlank()) {
            refreshAll();
            return;
        }
        sortBox.clear();
        List<Transaction> results = useCase.searchTransactions(keyword);
        showTransactions(results, "Hasil pencarian \"" + keyword.trim() + "\": "
                + results.size() + " transaksi", NOT_FOUND_TITLE, NOT_FOUND_HINT);
    }

    private void sortTransactions(SortOption option) {
        searchField.clear();
        List<Transaction> sorted = useCase.sortTransactions(option);
        showTransactions(sorted, "Diurutkan: " + presenter.formatSortOption(option),
                EMPTY_TITLE, EMPTY_HINT);
    }

    private void refreshAll() {
        searchField.clear();
        sortBox.clear();
        List<Transaction> all = useCase.getAllTransactions();
        showTransactions(all, all.size() + " transaksi tercatat", EMPTY_TITLE, EMPTY_HINT);
    }

    private void showTransactions(List<Transaction> transactions, String status,
                                  String emptyTitleText, String emptyHintText) {
        boolean empty = transactions.isEmpty();
        grid.setItems(transactions);
        grid.setVisible(!empty);
        emptyTitle.setText(emptyTitleText);
        emptyHint.setText(emptyHintText);
        emptyBox.setVisible(empty);
        statusLabel.setText(status);
        updateSummary();
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    // ------------------------------------------------------------------
    // Helper tampilan (warna dan ukuran ditetapkan eksplisit agar kontras terjamin)
    // ------------------------------------------------------------------

    private static Div column(String gap) {
        Div div = new Div();
        div.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", gap);
        return div;
    }

    private static Span block(Span span) {
        span.getStyle().set("display", "block");
        return span;
    }

    private static Section section(String headingId, String title, Component content) {
        H2 heading = new H2(title);
        heading.setId(headingId);
        heading.getStyle()
                .set("margin", "0 0 0.75rem")
                .set("font-size", "1.2rem")
                .set("color", INK);

        Section section = new Section(heading, content);
        section.getElement().setAttribute("aria-labelledby", headingId);
        return section;
    }

    /** Tombol terisi warna (aksi utama). Teks putih, kontras minimal 6:1. */
    private static Button solidButton(String text, VaadinIcon icon, String color,
                                      ComponentEventListener<ClickEvent<Button>> listener) {
        Button button = new Button(text, icon.create(), listener);
        button.getStyle()
                .set("background-color", color)
                .set("color", SURFACE)
                .set("font-weight", "600");
        return sizeForTouch(button);
    }

    /** Tombol dengan garis tepi warna (aksi sekunder). */
    private static Button outlineButton(String text, VaadinIcon icon, String color,
                                        ComponentEventListener<ClickEvent<Button>> listener) {
        Button button = new Button(text, icon.create(), listener);
        button.getStyle()
                .set("background-color", SURFACE)
                .set("color", color)
                .set("border", "1px solid " + color)
                .set("font-weight", "600");
        return sizeForTouch(button);
    }

    private static Button secondaryButton(String text, VaadinIcon icon,
                                          ComponentEventListener<ClickEvent<Button>> listener) {
        return outlineButton(text, icon, NAVY, listener);
    }

    /** Area sentuh minimal 44px agar nyaman di perangkat mobile. */
    private static Button sizeForTouch(Button button) {
        button.setMinHeight("44px");
        return button;
    }
}
