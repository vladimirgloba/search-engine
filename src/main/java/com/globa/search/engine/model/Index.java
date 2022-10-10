package com.globa.search.engine.model;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Entity
@Table(name = "index")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Index {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "page_id", nullable = false)
    private long page_id;

    @Column(name = "lemma_id ", nullable = false)
    private long lemma_id;

    @Column(name = "rank", nullable = false)
    private float rank;

    public Index() {
    }

    public Index(long page_id, long lemma_id, float rank) {
        this.page_id = page_id;
        this.lemma_id = lemma_id;
        this.rank = rank;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPage_id() {
        return page_id;
    }

    public void setPage_id(long page_id) {
        this.page_id = page_id;
    }

    public long getLemma_id() {
        return lemma_id;
    }

    public void setLemma_id(long lemma_id) {
        this.lemma_id = lemma_id;
    }

    public float getRank() {
        return rank;
    }

    public void setRank(float rank) {
        this.rank = rank;
    }

}
