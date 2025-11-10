create database credibanco;
drop table transaccion;
drop table tarjeta;


create table tarjeta (
  id bigserial primary key,
  numero_tarjeta char(16) unique not null, -- 16 dígitos, 6 primeros producto + aleatorios
  nombre_titular varchar(100) not null,   -- primer nombre y apellido
  fecha_creacion date not null,
  fecha_vencimiento char(6) not null,    -- formato mmaaaa, >= fecha creación + 3 años
  tipo_tarjeta varchar(10) not null,             -- 'credito' o 'debito'
  saldo decimal(15, 2) not null default 0 -- saldo en dólares con dos decimales
);


create table transaccion (
  id bigserial primary key,
  tarjeta_id bigint not null,
  monto decimal(15, 2) not null,
  fecha timestamp not null default current_timestamp,
  tipo_movimiento varchar(10) not null,  -- 'recarga' o 'compra'
  estado varchar(10) not null,           -- 'pendiente', 'realizada', 'anulada'

  constraint fk_tarjeta foreign key (tarjeta_id)
    references tarjeta (id)
    on delete cascade
);

-- índices para optimizar consultas
create index idx_tarjeta_numerotarjeta on tarjeta (numero_tarjeta);
create index idx_transaccion_tarjeta_id on transaccion (tarjeta_id);
create index idx_transaccion_estado on transaccion (estado);


insert into tarjeta (numero_tarjeta, nombre_titular, fecha_creacion, fecha_vencimiento, tipo_tarjeta, saldo) values
('1234567890123456', 'juan perez', '2025-11-08', '112028', 'credito', 1000000),
('6543219876543210', 'maria lopez', '2025-11-08', '112028', 'debito', 500000);


insert into transaccion (tarjeta_id, monto, fecha, tipo_movimiento, estado) values
(1, 200000, '2025-11-08 10:00:00', 'recarga', 'realizada'),
(1, 45000, '2025-11-08 11:00:00', 'compra', 'realizada'),
(2, 180000, '2025-11-08 12:00:00', 'recarga', 'realizada'),
(2, 32000, '2025-11-08 13:00:00', 'compra', 'pendiente');


select * from tarjeta;

select * from transaccion;

select * from transaccion
where tarjeta_id = 1
order by fecha desc;
