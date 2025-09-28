export interface Author {
  id: number;
  name: string;
  biography?: string;
  birthDate?: Date;
  nationality?: string;
  createdDate?: Date;
}

export interface AuthorCreateRequest {
  name: string;
  biography?: string;
  birthDate?: Date;
  nationality?: string;
}
