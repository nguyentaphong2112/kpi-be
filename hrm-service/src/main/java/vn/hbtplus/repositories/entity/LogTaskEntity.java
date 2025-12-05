package vn.hbtplus.repositories.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name="log_tasks")
public class LogTaskEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "log_task_id")
    private Long logTaskId;

    @Column(name = "project_code")
    private String projectCode;

    @Column(name = "name")
    private String name;

    @Column(name = "logDate")
    private Date logDate;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "total_house")
    private Long totalHouse;

    @Column(name = "telegram_chat_id")
    private Long telegramChatId;

}
