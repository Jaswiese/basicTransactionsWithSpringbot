CREATE TABLE IF NOT EXISTS TRANSACTIONS (
                                            id              UUID            DEFAULT random_uuid() PRIMARY KEY,
    amount          DECIMAL(15, 4)  NOT NULL,
    `timestamp`     TIMESTAMP       NOT NULL,
    reference       VARCHAR(255)    NOT NULL,
    bank_slogan          VARCHAR(255),
    input_user  VARCHAR(255)
    );