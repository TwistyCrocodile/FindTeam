package com.findteam.findteam.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "maintenance_task_executions")
@Getter
@Setter
@NoArgsConstructor
public class MaintenanceTaskExecution {

	@Id
	@Column(name = "task_key", nullable = false, length = 128)
	private String taskKey;

	@Column(nullable = false, updatable = false)
	private LocalDateTime startedAt;

	private LocalDateTime completedAt;

	private Integer totalRecipients;

	private Integer successfulSends;

	private Integer failedSends;

	public MaintenanceTaskExecution(String taskKey) {
		this.taskKey = taskKey;
	}

	@PrePersist
	protected void onCreate() {
		startedAt = LocalDateTime.now();
	}
}
