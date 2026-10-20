create database credibanco;

create table tarjeta (
  id bigserial primary key,
  numero_tarjeta char(16) unique not null,
  nombre_titular varchar(100) not null,
  fecha_creacion date not null,
  fecha_vencimiento char(6) not null,
  tipo_tarjeta varchar(10) not null,
  saldo decimal(15, 2) not null default 0,
  moneda varchar(3) not null default 'USD' check (moneda = 'USD')
);

create table transaccion (
  id bigserial primary key,
  tarjeta_id bigint not null,
  monto decimal(15, 2) not null,
  moneda varchar(3) not null default 'USD' check (moneda = 'USD'),
  fecha timestamp not null default current_timestamp,
  tipo_movimiento varchar(10) not null,
  estado varchar(10) not null check (estado in ('EXITOSA', 'RECHAZADA', 'ANULADA')),

  constraint fk_tarjeta foreign key (tarjeta_id)
    references tarjeta (id)
    on delete cascade
);

create index idx_tarjeta_numerotarjeta on tarjeta (numero_tarjeta);
create index idx_transaccion_tarjeta_id on transaccion (tarjeta_id);
create index idx_transaccion_estado on transaccion (estado);
 

insert into tarjeta (numero_tarjeta, nombre_titular, fecha_inicio, fecha_vencimiento, tipo_tarjeta, saldo, moneda) values
('1234567890123456', 'juan perez', '2025-11-08', '112028', 'CREDITO', 1000000.00, 'USD'),
('6543219876543210', 'maria lopez', '2025-11-08', '112028', 'DEBITO', 500000.00, 'USD');


insert into transaccion (tarjeta_id, monto, moneda, fecha, tipo_movimiento, estado) values
(1, 200000.00, 'USD', '2025-11-08 10:00:00', 'RECARGA', 'EXITOSA'),
(1, 45000.00, 'USD', '2025-11-08 11:00:00', 'COMPRA', 'EXITOSA'),
(2, 180000.00, 'USD', '2025-11-08 12:00:00', 'RECARGA', 'EXITOSA'),
(2, 32000.00, 'USD', '2025-11-08 13:00:00', 'COMPRA', 'EXITOSA');

select * from tarjeta;

select * from transaccion;

select * from transaccion 
where tarjeta_id = 1 
order by fecha desc;
