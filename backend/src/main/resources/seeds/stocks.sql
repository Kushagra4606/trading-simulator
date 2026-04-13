-- src/main/resources/seeds/stocks.sql
INSERT INTO stocks (symbol, name, sector, market_cap, base_price, circuit_limit_percent) VALUES
-- IT (10 stocks)
('TCS', 'Tata Consultancy Services', 'IT', 'LARGE', 3500.00, 10.0),
('INFY', 'Infosys', 'IT', 'LARGE', 1450.00, 10.0),
('WIPRO', 'Wipro', 'IT', 'LARGE', 420.00, 10.0),
('HCLTECH', 'HCL Technologies', 'IT', 'LARGE', 1200.00, 10.0),
('TECHM', 'Tech Mahindra', 'IT', 'MID', 1100.00, 10.0),
('LTIM', 'LTIMindtree', 'IT', 'MID', 4800.00, 10.0),
('MPHASIS', 'Mphasis', 'IT', 'MID', 2200.00, 10.0),
('COFORGE', 'Coforge', 'IT', 'MID', 5500.00, 10.0),
('PERSISTENT', 'Persistent Systems', 'IT', 'MID', 3800.00, 10.0),
('KPITTECH', 'KPIT Technologies', 'IT', 'SMALL', 1350.00, 15.0),

-- Banking (10 stocks)
('HDFCBANK', 'HDFC Bank', 'Banking', 'LARGE', 1550.00, 10.0),
('ICICIBANK', 'ICICI Bank', 'Banking', 'LARGE', 950.00, 10.0),
('SBIN', 'State Bank of India', 'Banking', 'LARGE', 620.00, 10.0),
('KOTAKBANK', 'Kotak Mahindra Bank', 'Banking', 'LARGE', 1700.00, 10.0),
('AXISBANK', 'Axis Bank', 'Banking', 'LARGE', 1050.00, 10.0),
('INDUSINDBK', 'IndusInd Bank', 'Banking', 'MID', 1400.00, 10.0),
('BANDHANBNK', 'Bandhan Bank', 'Banking', 'MID', 220.00, 15.0),
('FEDERALBNK', 'Federal Bank', 'Banking', 'MID', 155.00, 15.0),
('IDFCFIRSTB', 'IDFC First Bank', 'Banking', 'MID', 75.00, 15.0),
('RBLBANK', 'RBL Bank', 'Banking', 'SMALL', 185.00, 20.0),

-- Pharma (8 stocks)
('SUNPHARMA', 'Sun Pharmaceutical', 'Pharma', 'LARGE', 1100.00, 10.0),
('DRREDDY', 'Dr. Reddys Laboratories', 'Pharma', 'LARGE', 5200.00, 10.0),
('CIPLA', 'Cipla', 'Pharma', 'LARGE', 1250.00, 10.0),
('DIVISLAB', 'Divis Laboratories', 'Pharma', 'LARGE', 3600.00, 10.0),
('AUROPHARMA', 'Aurobindo Pharma', 'Pharma', 'MID', 950.00, 10.0),
('TORNTPHARM', 'Torrent Pharmaceuticals', 'Pharma', 'MID', 2600.00, 10.0),
('ALKEM', 'Alkem Laboratories', 'Pharma', 'MID', 4800.00, 10.0),
('NATCOPHARM', 'Natco Pharma', 'Pharma', 'SMALL', 920.00, 15.0),

-- Auto (8 stocks)
('MARUTI', 'Maruti Suzuki India', 'Auto', 'LARGE', 10500.00, 10.0),
('TATAMOTORS', 'Tata Motors', 'Auto', 'LARGE', 680.00, 10.0),
('M&M', 'Mahindra and Mahindra', 'Auto', 'LARGE', 1650.00, 10.0),
('BAJAJ-AUTO', 'Bajaj Auto', 'Auto', 'LARGE', 7200.00, 10.0),
('HEROMOTOCO', 'Hero MotoCorp', 'Auto', 'LARGE', 4100.00, 10.0),
('EICHERMOT', 'Eicher Motors', 'Auto', 'MID', 3700.00, 10.0),
('ASHOKLEY', 'Ashok Leyland', 'Auto', 'MID', 185.00, 15.0),
('TVSMOTOR', 'TVS Motor Company', 'Auto', 'MID', 2100.00, 10.0),

-- FMCG (8 stocks)
('HINDUNILVR', 'Hindustan Unilever', 'FMCG', 'LARGE', 2300.00, 10.0),
('ITC', 'ITC', 'FMCG', 'LARGE', 430.00, 10.0),
('NESTLEIND', 'Nestle India', 'FMCG', 'LARGE', 22000.00, 10.0),
('BRITANNIA', 'Britannia Industries', 'FMCG', 'LARGE', 4900.00, 10.0),
('DABUR', 'Dabur India', 'FMCG', 'MID', 520.00, 10.0),
('MARICO', 'Marico', 'FMCG', 'MID', 560.00, 10.0),
('GODREJCP', 'Godrej Consumer Products', 'FMCG', 'MID', 1050.00, 10.0),
('EMAMILTD', 'Emami', 'FMCG', 'SMALL', 490.00, 15.0),

-- Energy (6 stocks)
('RELIANCE', 'Reliance Industries', 'Energy', 'LARGE', 2450.00, 10.0),
('ONGC', 'Oil and Natural Gas Corp', 'Energy', 'LARGE', 240.00, 10.0),
('POWERGRID', 'Power Grid Corporation', 'Energy', 'LARGE', 290.00, 10.0),
('NTPC', 'NTPC', 'Energy', 'LARGE', 330.00, 10.0),
('BPCL', 'Bharat Petroleum', 'Energy', 'MID', 370.00, 10.0),
('ADANIGREEN', 'Adani Green Energy', 'Energy', 'MID', 1600.00, 15.0);