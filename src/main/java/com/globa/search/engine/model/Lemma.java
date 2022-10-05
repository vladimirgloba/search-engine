package com.globa.search.engine.model;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Entity
@Table(name = "lemma")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(name = "lemma")
    private String lemma;
    @Column(name = "frequency", nullable = false)
    private int frequency;
    @Column(name = "site_id")
    private long site_id;


    public Lemma() {
    }

    public Lemma( String lemma, int frequency,long site_id) {

        this.lemma = lemma;
        this.frequency = frequency;
        this.site_id=site_id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
