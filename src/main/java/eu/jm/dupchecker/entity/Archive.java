package eu.jm.dupchecker.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table
public class Archive {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String name;

	@Column
	private Long length;

	@Column(name = "last_modified")
	private Long lastModified;

	@Column(name = "partial_hash")
	private String partialHash;

	@Column(name = "full_hash")
	private String fullHash;

	@ManyToOne
	@JoinColumn(name = "directory_id")
	private Directory directory;

	// ------------------------------------------------------------------------

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getLength() {
		return length;
	}

	public void setLength(Long length) {
		this.length = length;
	}

	public Long getLastModified() {
		return lastModified;
	}

	public void setLastModified(Long lastModified) {
		this.lastModified = lastModified;
	}

	public String getFullHash() {
		return fullHash;
	}

	public void setFullHash(String fullHash) {
		this.fullHash = fullHash;
	}

	public String getPartialHash() {
		return partialHash;
	}

	public void setPartialHash(String partialHash) {
		this.partialHash = partialHash;
	}

	public Directory getDirectory() {
		return directory;
	}

	public void setDirectory(Directory directory) {
		this.directory = directory;
	}

//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj) {
//			return true;
//		}
//		if (obj == null || obj.getClass() != this.getClass()) {
//			return false;
//		}
//
//		Archive other = (Archive) obj;
//		// @formatter:off
//		return new EqualsBuilder()
//				.append(this.getId(), other.getId())
//				.append(this.getName(), other.getName())
//				.append(this.getLength(), other.getLength())
//				.append(this.getLastModified(), other.getLastModified())
//				.append(this.getPartialHash(), other.getPartialHash())
//				.append(this.getFullHash(), other.getFullHash())
//				.append(this.getDirectory(), other.getDirectory())
//					.isEquals();
//		// @formatter:on
//	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.getId()).append(this.getName()).append(this.getLength())
				.append(this.getLastModified()).append(this.getPartialHash()).append(this.getFullHash())
				.append(this.getDirectory().getId()).toHashCode();
	}

	@Override
	public String toString() {
		return "Archive [id=" + id + ", name=" + name + ", length=" + length + ", lastModified=" + lastModified
				+ ", partialHash=" + partialHash + ", fullHash=" + fullHash + ", directory=" + directory.getId() + "]";
	}

}
