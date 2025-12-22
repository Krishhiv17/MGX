package com.mgx.game.dto;

import com.mgx.game.model.GameStatus;

public class UpdateGameStatusRequest {
  private GameStatus status;

  public GameStatus getStatus() {
    return status;
  }

  public void setStatus(GameStatus status) {
    this.status = status;
  }
}
