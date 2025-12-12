CREATE TABLE users (
    id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE books (
    id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    publication_date DATE NOT NULL,
    copies INTEGER NOT NULL,
    
    CONSTRAINT pk_books PRIMARY KEY (id)
);

CREATE TABLE loans (
    id UUID NOT NULL,
    loan_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    user_id UUID NOT NULL,
    book_id UUID NOT NULL,
    
    CONSTRAINT pk_loans PRIMARY KEY (id),
    
    CONSTRAINT fk_loans_user
        FOREIGN KEY (user_id) 
        REFERENCES users (id),
        
    CONSTRAINT fk_loans_book
        FOREIGN KEY (book_id) 
        REFERENCES books (id)
);