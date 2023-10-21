package com.example.onlinebookstore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity
@SQLDelete(sql = "UPDATE is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;
    @NotNull
    private int quantity;
    @NotNull
    private BigDecimal price;
    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    public void addOrder(Order order) {
        setOrder(order);
        order.getOrderItems().add(this);
    }
}
