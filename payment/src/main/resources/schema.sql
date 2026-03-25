create table if not exists PAYMENTSTATUS
(
    payment_id varchar(64)  not null primary key,
    amount     numeric(20, 2) not null,
    reference  varchar(128) not null,
    status     varchar(32)  not null
);

create table if not exists TOKENENTRY
(
    segment       integer      not null,
    processorName varchar(255) not null,
    token         bytea,
    tokenType     varchar(255),
    timestamp     varchar(1000),
    owner         varchar(1000),
    primary key (processorName, segment)
    );
