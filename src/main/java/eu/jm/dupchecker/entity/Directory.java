package eu.jm.dupchecker.entity;

import java.util.Collections;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.util.CollectionUtils;

@Entity
public class Directory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "canonical_path")
	private String canonicalPath;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "directory")
	private Set<Archive> files;

	// ------------------------------------------------------------------------

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCanonicalPath() {
		return canonicalPath;
	}

	public void setCanonicalPath(String canonicalPath) {
		this.canonicalPath = canonicalPath;
	}

	/**
	 * 
	 * @return unmodifiable set of files. If there are no files an empty set is
	 *         return. This method will never return <code>null</code>.
	 */
	public Set<Archive> getFiles() {
		if (CollectionUtils.isEmpty(this.files)) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(this.files);
	}

	public void setFiles(Set<Archive> files) {
		this.files = files;
	}

	// ------------------------------------------------------------------------

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}

		Directory other = (Directory) obj;
		return new EqualsBuilder().append(this.getId(), other.getId())
				.append(this.getCanonicalPath(), other.getCanonicalPath()).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.getId()).append(this.getCanonicalPath()).toHashCode();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Directory [id=").append(id).append(", canonicalPath=").append(canonicalPath).append(", files=")
				.append(files).append("]");
		return builder.toString();
	}

}
