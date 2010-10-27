package org.sakaiproject.nakamura.api.lite.accesscontrol;


public class AclModification {
	  enum Operation {
		  OP_REPLACE(),
		  OP_OR(),
		  OP_AND(),
		  OP_XOR(),
		  OP_NOT(),
		  OP_DEL()
	  }
	private String key;
	private int bitmap;
	private Operation op;;
	    
	public AclModification(String key, int bitmap, Operation op) {
		this.key = key;
		this.bitmap = bitmap;
		this.op = op;
	}
	
	public String getAceKey() {
		return this.key;
	}
	
	public int modify(int bits) {
		switch(this.op) {
		case OP_REPLACE:
			return this.bitmap;
		case OP_OR:
			return bits | this.bitmap;
		case OP_AND:
			return bits & this.bitmap;
		case OP_XOR:
			return this.bitmap ^ bits;
		case OP_NOT:
			return ~this.bitmap;
		}
		return this.bitmap;
	}
	public boolean isRemove() {
		return this.op.equals(Operation.OP_DEL);
	}
}
