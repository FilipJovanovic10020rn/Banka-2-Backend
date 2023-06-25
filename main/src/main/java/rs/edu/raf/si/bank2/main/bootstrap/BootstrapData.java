package rs.edu.raf.si.bank2.main.bootstrap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletContextListener;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rs.edu.raf.si.bank2.main.bootstrap.readers.CurrencyReader;
import rs.edu.raf.si.bank2.main.exceptions.CurrencyNotFoundException;
import rs.edu.raf.si.bank2.main.models.mariadb.*;
import rs.edu.raf.si.bank2.main.models.mariadb.Currency;
import rs.edu.raf.si.bank2.main.repositories.mariadb.*;
import rs.edu.raf.si.bank2.main.services.ForexService;
import rs.edu.raf.si.bank2.main.services.OptionService;
import rs.edu.raf.si.bank2.main.services.StockService;

@Component
public class BootstrapData implements CommandLineRunner {

    public static final String forexApiKey = "OF6BVKZOCXWHD9NS";
    /**
     * TODO promeniti ovo pre produkcije. Promenjen admin mejl da bismo mu
     * zapravo imali pristup. Mogu
     * da podesim forwardovanje ako je potrebno nekom drugom jos pristup.
     */
    private static final String ADMIN_EMAIL = "anesic3119rn+banka2backend" + "+admin@raf.rs";

    private final Logger logger = LoggerFactory.getLogger(CommandLineRunner.class);
    /**
     * TODO promeniti password ovde da bude jaci! Eventualno TODO napraviti
     * da se auto-generise novi
     * password pri TODO svakoj migraciji.
     */
    private final UserRepository userRepository;

    private final PermissionRepository permissionRepository;
    private final CurrencyRepository currencyRepository;
    private final InflationRepository inflationRepository;
    private final PasswordEncoder passwordEncoder;
    private final ExchangeRepository exchangeRepository;
    private final FutureRepository futureRepository;
    private final BalanceRepository balanceRepository;
    private final UserStocksRepository userStocksRepository;
    private final ForexService forexService;
    private final StockRepository stockRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final StockService stockService;
    private final OptionService optionService;
    private final OptionRepository optionRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final RedisConnectionFactory redisConnectionFactory;

    private boolean runTestSetup = true;

    @Autowired
    public BootstrapData(
            UserRepository userRepository,
            PermissionRepository permissionRepository,
            CurrencyRepository currencyRepository,
            InflationRepository inflationRepository,
            PasswordEncoder passwordEncoder,
            ExchangeRepository exchangeRepository,
            FutureRepository futureRepository,
            BalanceRepository balanceRepository,
            UserStocksRepository userStocksRepository,
            ForexService forexService,
            StockRepository stockRepository,
            StockHistoryRepository stockHistoryRepository,
            StockService stockService,
            OptionService optionService,
            OptionRepository optionRepository,
            EntityManagerFactory entityManagerFactory,
            RedisConnectionFactory redisConnectionFactory) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.currencyRepository = currencyRepository;
        this.inflationRepository = inflationRepository;
        this.passwordEncoder = passwordEncoder;
        this.exchangeRepository = exchangeRepository;
        this.futureRepository = futureRepository;
        this.balanceRepository = balanceRepository;
        this.userStocksRepository = userStocksRepository;
        this.forexService = forexService;
        this.stockRepository = stockRepository;
        this.stockHistoryRepository = stockHistoryRepository;
        this.stockService = stockService;
        this.optionService = optionService;
        this.optionRepository = optionRepository;
        this.entityManagerFactory = entityManagerFactory;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public void run(String... args) throws Exception {

        boolean temporaryLoad = true; // TODO OVO TREBA DA SE UNAPREDI I DA BUDE NA FALSE NA LOKALU

        if (!temporaryLoad) {
            // todo nameti da ovo radi samo za testiranje
            List<Balance> listOfBalances = balanceRepository.findAll();
            if (listOfBalances.size() == 0) runTestSetup = true;
            // If empty, add futures in db from csv
            if (this.futureRepository.count() == 0) {
                logger.info("Added futures");
                this.loadFutureTable();
            }
            // If empty, add currencies in db from csv
            if (this.currencyRepository.count() == 0) {
                logger.info("Added currencies");
                this.loadCurrenciesAndInflationTable();
            }
            // If empty, add exchange markets in db from csv
            if (this.exchangeRepository.count() == 0) {
                logger.info("Added exchange markets");
                this.loadExchangeMarkets();
            }
            // If empty, add stocks in db from csv
            if (stockRepository.count() == 0) {
                logger.info("Adding stocks");
                loadStocksTable();
            }
            //            if (runTestSetup) {
            //            addAdminForTest();//
            //            addBalancesToAdmin();
            //            runTestSetup = false;
            //            }
        } else {
            this.temporaryLoad();
        }

        logger.info("Started!");
        System.out.println("Everything started");
    }

    private void addBalancesToAdmin() {
        // Add initial 100_000 RSD to admin
        Optional<User> adminUser = userRepository.findUserByEmail(ADMIN_EMAIL);
        User admin = adminUser.get();
        Balance balance1 = this.getInitialAdminBalance(admin, "RSD");
        Balance balance2 = this.getInitialAdminBalance(admin, "USD");
        List<Balance> balances = new ArrayList<>();
        balances.add(balance1);
        balances.add(balance2);
        admin.setBalances(balances);
        this.userRepository.save(admin);
        this.balanceRepository.save(balance1);
        this.balanceRepository.save(balance2);
        giveAdminStocks(admin);
    }

    private void giveAdminStocks(User user) { // todo popravi
        Stock stock = stockService.getStockBySymbol("AAPL");
        Stock stock2 = stockService.getStockBySymbol("GOOGL");
        UserStock userStock = new UserStock(0L, user, stock, 100, 0);
        UserStock userStock2 = new UserStock(0L, user, stock2, 100, 0);
        userStocksRepository.save(userStock);
        userStocksRepository.save(userStock2);
    }

    private Balance getInitialAdminBalance(User admin, String currency) {
        Balance balance = new Balance();
        balance.setUser(admin);
        Optional<rs.edu.raf.si.bank2.main.models.mariadb.Currency> curr =
                this.currencyRepository.findCurrencyByCurrencyCode(currency);
        if (curr.isEmpty()) throw new CurrencyNotFoundException(currency);
        balance.setCurrency(curr.get());
        balance.setAmount(100000f);
        balance.setFree(100000f);
        balance.setReserved(0f);
        balance.setType(BalanceType.CASH);
        return balance;
    }

    private void loadCurrenciesAndInflationTable() throws IOException {
        CurrencyReader cs = new CurrencyReader();
        List<Currency> currencyList = cs.getCurrencies();
        this.currencyRepository.saveAll(currencyList);
        List<Inflation> inflationList = cs.getInflations();
        this.inflationRepository.saveAll(inflationList);
    }

    private void loadExchangeMarkets() throws IOException {
        // Do this only on the first ever run of the app.
        // read from file

        String resPath = "csvs/exchange.csv";
        URL url = ServletContextListener.class.getClassLoader().getResource(resPath);
        if (url == null) {
            logger.error("Could not find resource: " + resPath);
            return;
        }

        URI uri;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        List<Exchange> exchanges = Files.lines(Paths.get(uri))
                .parallel()
                .skip(1)
                .map(line -> line.split(","))
                .filter(data ->
                        exchangeRepository.findExchangeByMicCode(data[2]).isEmpty())
                .map(data -> new Exchange(
                        data[0],
                        data[1],
                        data[2],
                        data[3],
                        this.currencyRepository
                                        .findCurrencyByCurrencyCode(data[4])
                                        .isPresent()
                                ? this.currencyRepository
                                        .findCurrencyByCurrencyCode(data[4])
                                        .get()
                                : null,
                        data[5],
                        data[6],
                        data[7]))
                .toList();

        // save into repository
        for (int i = 0; i < exchanges.size() - 1; i++) {
            for (int j = i + 1; j < exchanges.size(); j++) {
                Exchange e = exchanges.get(i);
                Exchange e1 = exchanges.get(j);
                if (Objects.equals(e.getAcronym(), e1.getAcronym())) {
                    logger.info("id " + e.getId() + "id " + e1.getId());
                }
            }
        }
        exchangeRepository.saveAll(exchanges);
    }

    private void loadFutureTable() throws IOException, ParseException { //
        // todo promeni da ucitava sa id
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yyyy");
        String formattedDate = dateFormat.format(new Date());

        String resPath = "csvs/future_data.csv";
        URL url = ServletContextListener.class.getClassLoader().getResource(resPath);
        if (url == null) {
            logger.error("Could not find resource: " + resPath);
            return;
        }

        URI uri;
        try {
            uri = url.toURI();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        // TODO intellij kaze da treba dodati try-catch
        List<Future> futures = Files.lines(Paths.get(uri))
                .parallel()
                .skip(1)
                .map(line -> line.split(","))
                .filter(data -> futureRepository.findFutureByFutureName(data[0]).isEmpty())
                .map(data -> new Future(
                        data[0],
                        Integer.parseInt(data[1]),
                        data[2],
                        Integer.parseInt(data[3]),
                        data[4],
                        formattedDate,
                        true))
                .toList();

        futureRepository.saveAll(futures);

        randomiseFutureTableData();
    }

    private void randomiseFutureTableData() throws ParseException { //
        // calendar.add(Calendar.MONTH, 1);
        List<Future> allFutures = futureRepository.findAll();
        List<Future> newRandomisedFutures = new ArrayList<>();
        Random randomGenerator = new Random();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yyyy");
        Calendar calendar = Calendar.getInstance();
        int dateIncreaseAmount = 1;
        int moneyDecreased;

        for (Future future : allFutures) {
            moneyDecreased = future.getMaintenanceMargin();

            for (int i = 0; i <= randomGenerator.nextInt(6) + 4; i++) {
                Future newFuture = new Future(future);
                calendar.setTime(dateFormat.parse(newFuture.getSettlementDate()));
                calendar.add(Calendar.MONTH, dateIncreaseAmount++);
                newFuture.setSettlementDate(dateFormat.format(calendar.getTime()));
                moneyDecreased -= randomGenerator.nextInt(200) + 100;
                if (moneyDecreased <= 0) {
                    moneyDecreased = randomGenerator.nextInt(200) + 100;
                }
                newFuture.setMaintenanceMargin(moneyDecreased);
                newRandomisedFutures.add(newFuture);
            }
            dateIncreaseAmount = 1;
        }

        futureRepository.saveAll(newRandomisedFutures);
    }

    private void loadStocksTable() throws IOException {

        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Session session = sessionFactory.openSession();

        String resPath = "stocks.csv";
        URL url = ServletContextListener.class.getClassLoader().getResource(resPath);
        if (url == null) {
            logger.error("Could not find resource: " + resPath);
            return;
        }

        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(url.getPath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        String header = br.readLine();
        String line = br.readLine();

        while (line != null) {

            String[] data = line.split(",");

            Optional<Exchange> exchange = exchangeRepository.findExchangeByAcronym(data[12]);
            Exchange mergedExchange = (Exchange) session.merge(exchange.get());

            Stock stock = Stock.builder()
                    .companyName(data[0])
                    .outstandingShares(Long.valueOf(data[1]))
                    .dividendYield(new BigDecimal(data[2]))
                    .openValue(new BigDecimal(data[3]))
                    .highValue(new BigDecimal(data[4]))
                    .lowValue(new BigDecimal(data[5]))
                    .priceValue(new BigDecimal(data[6]))
                    .volumeValue(Long.valueOf(data[7]))
                    .lastUpdated(LocalDate.now())
                    .previousClose(new BigDecimal(data[9]))
                    .changeValue(new BigDecimal(data[10]))
                    .changePercent(data[11])
                    .exchange(mergedExchange)
                    .symbol(data[13])
                    .websiteUrl(data[14])
                    .build();

            session.beginTransaction();
            session.persist(stock);
            session.getTransaction().commit();

            line = br.readLine();
        }

        br.close();

        for (Stock s : stockRepository.findAll()) {

            resPath = "stock_history.csv";
            url = ServletContextListener.class.getClassLoader().getResource(resPath);
            if (url == null) {
                logger.error("Could not find resource: " + resPath);
                return;
            }

            Stock mergedStock = (Stock) session.merge(s);
            BufferedReader br1;
            try {
                br1 = new BufferedReader(new FileReader(url.getPath()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }

            String header1 = br1.readLine();
            String line1 = br1.readLine();

            while (line1 != null) {

                String[] data = line1.split(",");
                String symbol = data[6];

                if (symbol.equals(s.getSymbol())) {
                    StockHistory stockHistory = StockHistory.builder()
                            .openValue(new BigDecimal(data[0]))
                            .highValue(new BigDecimal(data[1]))
                            .lowValue(new BigDecimal(data[2]))
                            .closeValue(new BigDecimal(data[3]))
                            .volumeValue(Long.valueOf(data[4]))
                            .onDate(
                                    data[5].contains(" ")
                                            ? LocalDateTime.parse(
                                                    data[5], DateTimeFormatter.ofPattern("yyyy" + "-MM-dd HH:mm:ss"))
                                            : LocalDateTime.parse(
                                                    data[5] + " 00:00:00",
                                                    DateTimeFormatter.ofPattern("yyyy" + "-MM-dd HH:mm:ss")))
                            .stock(mergedStock)
                            .type(StockHistoryType.valueOf(data[7]))
                            .build();

                    session.beginTransaction();
                    session.persist(stockHistory);
                    session.getTransaction().commit();
                }
                line1 = br1.readLine();
            }

            br1.close();
        }
    }

    private void addAdminForTest() {

        Optional<User> adminCheck = userRepository.findUserByEmail(ADMIN_EMAIL);
        if (adminCheck.isPresent()) return;

        final String ADMIN_PASS = "admin";
        final String ADMIN_FNAME = "Admin";
        final String ADMIN_LNAME = "Adminic";
        final String ADMIN_JMBG = "2902968000000";
        final String ADMIN_PHONE = "0657817522";
        final String ADMIN_JOB = "ADMINISTRATOR";
        final boolean ADMIN_ACTIVE = true;

        // Set up all permissions
        Set<PermissionName> allPermissions = EnumSet.allOf(PermissionName.class);
        System.out.println(allPermissions);

        for (PermissionName pn : allPermissions) {
            List<Permission> findPerm = permissionRepository.findByPermissionNames(Collections.singletonList(pn));

            if (!findPerm.isEmpty()) {
                logger.info("Permission " + pn + " already found");
                continue;
            }

            Permission addPerm = new Permission(pn);
            this.permissionRepository.save(addPerm);
            logger.info("Permission " + pn + "added");
        }

        // Set up admin user
        Optional<User> adminUser = userRepository.findUserByEmail(ADMIN_EMAIL);
        if (adminUser.isPresent()) {
            logger.info("Root admin already added");
            return;
        }

        // Build root user object
        User admin = User.builder()
                .email(ADMIN_EMAIL)
                .firstName(ADMIN_FNAME)
                .lastName(ADMIN_LNAME)
                .password(this.passwordEncoder.encode(ADMIN_PASS))
                .jmbg(ADMIN_JMBG)
                .phone(ADMIN_PHONE)
                .jobPosition(ADMIN_JOB)
                .active(ADMIN_ACTIVE)
                .dailyLimit(1000000d) // usd
                // .defaultDailyLimit(10000D) // usd
                .defaultDailyLimit(1000000d) // usd
                .build();

        // Set admin's perms
        List<Permission> permissions = new ArrayList<>();
        permissions.add(permissionRepository
                .findByPermissionNames(Collections.singletonList(PermissionName.ADMIN_USER))
                .get(0));
        admin.setPermissions(permissions);

        // Save admin
        this.userRepository.save(admin);
        logger.info("Root admin added");
    }

    /*
    future
    currency
    exchange
    stocks
     */
    private void temporaryLoad() {

        // corn,5000,bushel,1600,AGRICULTURE
        // soybean,5000,bushel,2700,AGRICULTURE

        futureRepository.save(new Future(1L, "corn", 5000, "bushel", 1600, "AGRICULTURE", "01.05.2025", true, null));
        futureRepository.save(new Future(2L, "corn", 5000, "bushel", 1500, "AGRICULTURE", "01.03.2025", true, null));
        futureRepository.save(new Future(3L, "soybean", 5000, "bushel", 1700, "AGRICULTURE", "01.05.2025", true, null));
        futureRepository.save(new Future(4L, "soybean", 5000, "bushel", 1750, "AGRICULTURE", "01.04.2025", true, null));

        currencyRepository.save(
                new Currency(1L, "United States Dollar", "USD", "$", "United States", new ArrayList<>()));
        currencyRepository.save(new Currency(2L, "Danish Krone", "DKK", "DKK", "Denmark", new ArrayList<>()));
        currencyRepository.save(new Currency(3L, "Serbian Dinar", "RSD", "RSD", "Serbia", new ArrayList<>()));

        exchangeRepository.save(
                new Exchange(1L, "New York Stock Exchange", "NYSE", "XNYS", "USA", null, "USA", " 09:30", " 16:00"));
        exchangeRepository.save(
                new Exchange(2L, "Nasdaq", "NASDAQ", "XNAS", "USA", null, "America/New_York", " 09:30", " 16:00"));

        //        giveAdminStocks();

        // dodajemo 5 opcija
        //        optionService.getFiveMostImportantOptionsFromApi();

        // dodajemo 5 stockova
        stockService.updateAllStocksInDb();

        if (balanceRepository.findAllByUser_Id(1L).size() == 0) addBalancesToAdmin();

        // todo stock history
    }
}
