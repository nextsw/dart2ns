package gqltosql.schema;

public abstract class DField<T, R> {
	
	private static final byte READANDWRITE = 0x0;
	private static final byte WRITEONCE = 0x1;
	private static final byte READONLY = 0x2;
	private static final byte LOCAL = 0x4;
	private static final byte NONE = 0x8;

	private int index;
	private String name;
	private String column;
	private DModel<?> ref;
	private String collTable;
	private String mappedByColumn;
	private DModel<T> decl;
	private boolean notNull;
	private boolean transientField;
	
	private byte readType = READANDWRITE;

	public DField(DModel<T> decl, int index, String name, String column) {
		this.decl = decl;
		this.index = index;
		this.name = name;
		this.column = column;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public int getEnumType() {
		return 0;
	}

	public abstract FieldType getType();

	public abstract FieldPrimitiveType getPrimitiveType();

	public DModel<?> getReference() {
		return ref;
	}

	public void setRef(DModel<?> ref) {
		this.ref = ref;
	}

	public String getColumnName() {
		return column;
	}

	public void setCollTable(String collTable) {
		this.collTable = collTable;
	}

	public String getCollTableName(String parentTable) {
		return collTable;
	}

	public DModel<T> declType() {
		return decl;
	}

	public String getMappedByColumn() {
		return mappedByColumn;
	}

	public boolean isChild() {
		return false;
	}

	@Override
	public String toString() {
		return name;
	}
	
	public boolean isNotNull() {
		return notNull;
	}

	public DField<?, ?> notNull() {
		this.notNull = true;
		return this;
	}
	
	public DField<T, R> writeOnce() {
		this.readType = WRITEONCE;
		return this;
	}
	
	public DField<T, R> readOnly() {
		this.readType = READONLY;
		return this;
	}
	
	public DField<T, R> markLocal() {
		this.readType = LOCAL;
		return this;
	}
	
	public DField<T, R> markNone() {
		this.readType = NONE;
		return this;
	}
	
	public boolean canSend() {
		return this.readType == READANDWRITE || this.readType == WRITEONCE || this.readType == READONLY;
	}
	
	public boolean canReceive() {
		return this.readType == READANDWRITE || this.readType == WRITEONCE;
	}

	public boolean isTransientField() {
		return transientField;
	}
	
	public DField<T, R> markTransient() {
		this.transientField= true;
		return this;
	}
	
	public abstract Object getValue(T _this);

	public abstract Object fetchValue(T _this, IDataFetcher fetcher);

	public abstract void setValue(T _this, R value);
}
