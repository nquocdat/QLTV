import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ChatAi } from './chat-ai';

describe('ChatAi', () => {
  let component: ChatAi;
  let fixture: ComponentFixture<ChatAi>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChatAi]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ChatAi);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
