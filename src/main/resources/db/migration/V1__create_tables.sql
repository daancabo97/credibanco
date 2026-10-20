create table tarjeta (
  id bigserial primary key,
  numero_tarjeta varchar(16) unique not null,
  nombre_titular varchar(255) not null,
  fecha_creacion date not null,
  fecha_vencimiento date not null,
  tipo_tarjeta varchar(16) not null check (tipo_tarjeta in ('CREDITO', 'DEBITO')),
  saldo decimal(15, 2) not null default 0,
  moneda varchar(3) not null default 'USD' check (moneda = 'USD')
);

create table transaccion (
  id bigserial primary key,
  tarjeta_id bigint not null,
  monto decimal(15, 2) not null,
  moneda varchar(3) not null default 'USD' check (moneda = 'USD'),
  fecha timestamp not null,
  tipo_movimiento varchar(10) not null check (tipo_movimiento in ('COMPRA', 'RECARGA')),
  estado varchar(10) not null check (estado in ('EXITOSA', 'RECHAZADA', 'ANULADA')),
  constraint fk_tarjeta foreign key (tarjeta_id) references tarjeta (id)
);

create index idx_tarjeta_numerotarjeta on tarjeta (numero_tarjeta);
create index idx_transaccion_tarjeta_id on transaccion (tarjeta_id);
create index idx_transaccion_estado on transaccion (estado);
