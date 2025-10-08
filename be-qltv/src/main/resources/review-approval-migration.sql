-- Add review approval system
-- Run this SQL to add approval fields to existing reviews table

-- Add approved column (default false for existing reviews)
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS approved BOOLEAN NOT NULL DEFAULT FALSE;

-- Add loan_id reference column
ALTER TABLE reviews ADD COLUMN IF NOT EXISTS loan_id BIGINT;

-- Add foreign key constraint
ALTER TABLE reviews 
ADD CONSTRAINT fk_reviews_loan 
FOREIGN KEY (loan_id) REFERENCES loans(id) 
ON DELETE SET NULL;

-- Create index for better performance
CREATE INDEX IF NOT EXISTS idx_reviews_approved ON reviews(approved);
CREATE INDEX IF NOT EXISTS idx_reviews_loan_id ON reviews(loan_id);
CREATE INDEX IF NOT EXISTS idx_reviews_book_approved ON reviews(book_id, approved);

-- Sample: Approve existing reviews (optional)
-- UPDATE reviews SET approved = TRUE WHERE id > 0;
